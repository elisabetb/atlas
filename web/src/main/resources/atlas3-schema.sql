-- Microarray differential analytics
DROP TABLE MICROARRAY_DIFF_ANALYTICS;
CREATE TABLE MICROARRAY_DIFF_ANALYTICS (
    DESIGNELEMENT VARCHAR2(255) NOT NULL
  , EXPERIMENT VARCHAR2(255) NOT NULL
  , ARRAYDESIGN  VARCHAR(255) NOT NULL
  , CONTRASTID VARCHAR2(255) NOT NULL
  , ISACTIVE VARCHAR2(1) DEFAULT 'T'
  , PVAL FLOAT(125) NOT NULL
  , LOG2FOLD FLOAT(125)
  , TSTAT FLOAT(125) 
)
PARTITION BY LIST (ISACTIVE)
SUBPARTITION BY RANGE (PVAL) 
( PARTITION DIFF_ANALYTICS_ACTIVE VALUES ('T')
       ( SUBPARTITION BELOW_DEFAULT_FDR VALUES LESS THAN (0.05)
       , SUBPARTITION OTHERS VALUES LESS THAN (MAXVALUE)
       )
, PARTITION DIFF_ANALYTICS_INACTIVE VALUES ('F')
 )
PARALLEL;

CREATE INDEX PK_MICROARRAY_DIFF_ANALYTICS ON MICROARRAY_DIFF_ANALYTICS (DESIGNELEMENT, EXPERIMENT, CONTRASTID, PVAL, ISACTIVE) TABLESPACE ATLASPRD3_INDX;
ALTER TABLE MICROARRAY_DIFF_ANALYTICS
  ADD CONSTRAINT PK_MICROARRAY_DIFF_ANALYTICS
  PRIMARY KEY (DESIGNELEMENT, EXPERIMENT, CONTRASTID, PVAL, ISACTIVE) USING INDEX PK_MICROARRAY_DIFF_ANALYTICS
ENABLE;

CREATE INDEX MICROARRAY_DIFF_ANALYTICS_IDX ON MICROARRAY_DIFF_ANALYTICS (EXPERIMENT, CONTRASTID) COMPRESS LOCAL (
     PARTITION  DIFF_ANALYTICS_ACTIVE TABLESPACE ATLASPRD3_INDX
     (
	SUBPARTITION BELOW_DEFAULT_FDR, 
	SUBPARTITION OTHERS
     )
   , PARTITION  DIFF_ANALYTICS_INACTIVE TABLESPACE ATLASPRD3_INDX
);

-- RNA-seq differential analytics
DROP TABLE RNASEQ_DIFF_ANALYTICS;
CREATE TABLE RNASEQ_DIFF_ANALYTICS (
    IDENTIFIER VARCHAR2(255) NOT NULL
  , EXPERIMENT VARCHAR2(255) NOT NULL
  , CONTRASTID VARCHAR2(255) NOT NULL
  , ISACTIVE VARCHAR2(1) DEFAULT 'T'
  , PVAL FLOAT(125) NOT NULL
  , LOG2FOLD FLOAT(125) 
)
PARTITION BY LIST (ISACTIVE)
SUBPARTITION BY RANGE (PVAL) 
( PARTITION DIFF_ANALYTICS_ACTIVE VALUES ('T')
       ( SUBPARTITION BELOW_DEFAULT_FDR VALUES LESS THAN (0.05)
       , SUBPARTITION OTHERS VALUES LESS THAN (MAXVALUE)
       )
, PARTITION DIFF_ANALYTICS_INACTIVE VALUES ('F')
 )
PARALLEL;

CREATE INDEX PK_RNASEQ_DIFF_ANALYTICS ON RNASEQ_DIFF_ANALYTICS (IDENTIFIER, EXPERIMENT, CONTRASTID, PVAL, ISACTIVE) TABLESPACE ATLASPRD3_INDX;
ALTER TABLE RNASEQ_DIFF_ANALYTICS
  ADD CONSTRAINT PK_RNASEQ_DIFF_ANALYTICS
  PRIMARY KEY (IDENTIFIER, EXPERIMENT, CONTRASTID, PVAL, ISACTIVE) USING INDEX PK_RNASEQ_DIFF_ANALYTICS
ENABLE;

CREATE INDEX RNASEQ_DIFF_ANALYTICS_IDX ON RNASEQ_DIFF_ANALYTICS (EXPERIMENT, CONTRASTID) COMPRESS LOCAL (
     PARTITION  DIFF_ANALYTICS_ACTIVE TABLESPACE ATLASPRD3_INDX
     (
	SUBPARTITION BELOW_DEFAULT_FDR, 
	SUBPARTITION OTHERS
     )
   , PARTITION  DIFF_ANALYTICS_INACTIVE TABLESPACE ATLASPRD3_INDX
);

-- Differential analitics across all types of experiments, with additional gene name and organism information
CREATE OR REPLACE FORCE VIEW VW_DIFFANALYTICS (IDENTIFIER, DESIGNELEMENT, NAME, ORGANISM, EXPERIMENT, CONTRASTID, ISACTIVE, PVAL, LOG2FOLD, TSTAT) AS
select dem.IDENTIFIER, mda.DESIGNELEMENT, bn.NAME, o.NAME, mda.EXPERIMENT, mda.CONTRASTID, mda.ISACTIVE, mda.PVAL, mda.LOG2FOLD, mda.TSTAT 
from MICROARRAY_DIFF_ANALYTICS subpartition( BELOW_DEFAULT_FDR ) mda
join DESIGNELEMENT_MAPPING dem on mda.designelement=dem.designelement and mda.arraydesign = dem.arraydesign
join BIOENTITY_NAME bn on dem.identifier=bn.identifier  
join BIOENTITY_ORGANISM o on bn.organismid = o.organismid
union all
select rda.IDENTIFIER, null, bn.NAME, o.name, rda.EXPERIMENT, rda.CONTRASTID, rda.ISACTIVE, rda.PVAL, rda.LOG2FOLD, null 
from RNASEQ_DIFF_ANALYTICS subpartition( BELOW_DEFAULT_FDR ) rda
join BIOENTITY_NAME bn on rda.IDENTIFIER=bn.identifier
join BIOENTITY_ORGANISM o on bn.organismid = o.organismid;

-- RNA-seq baseline analytics
DROP TABLE RNASEQ_BSLN_EXPRESSIONS;
CREATE TABLE RNASEQ_BSLN_EXPRESSIONS (
    IDENTIFIER VARCHAR2(255) NOT NULL
  , EXPERIMENT VARCHAR2(255) NOT NULL
  , ASSAYGROUPID VARCHAR2(255) NOT NULL
  , ISACTIVE VARCHAR2(1) DEFAULT 'T'
  , EXPRESSION FLOAT(125) NOT NULL
)
PARTITION BY LIST (ISACTIVE)
SUBPARTITION BY RANGE (ASSAYGROUPID) 
( PARTITION BSLN_EXPRESSIONS_ACTIVE VALUES ('T')
       ( SUBPARTITION OTHERS VALUES LESS THAN (0.6)
       , SUBPARTITION ABOVE_CUTOFF VALUES LESS THAN (MAXVALUE)
       )
, PARTITION BSLN_EXPRESSIONS_INACTIVE VALUES ('F')
 )
PARALLEL;

CREATE INDEX PK_RNASEQ_BSLN_ANALYTICS ON RNASEQ_BSLN_EXPRESSIONS (IDENTIFIER, EXPERIMENT, ASSAYGROUPID, EXPRESSION, ISACTIVE) TABLESPACE ATLASPRD3_INDX;
ALTER TABLE RNASEQ_BSLN_EXPRESSIONS
  ADD CONSTRAINT PK_RNASEQ_BSLN_EXPRESSIONS
  PRIMARY KEY (IDENTIFIER, EXPERIMENT, ASSAYGROUPID, EXPRESSION, ISACTIVE) USING INDEX PK_RNASEQ_BSLN_ANALYTICS
ENABLE;

CREATE INDEX RNASEQ_BSLN_EXPRESSIONS_IDX ON RNASEQ_BSLN_EXPRESSIONS (IDENTIFIER, ISACTIVE, EXPERIMENT, ASSAYGROUPID) COMPRESS LOCAL (
     PARTITION  BSLN_EXPRESSIONS_ACTIVE TABLESPACE ATLASPRD3_INDX
     (
        SUBPARTITION OTHERS,
	SUBPARTITION ABOVE_CUTOFF
     )
   , PARTITION BSLN_EXPRESSIONS_INACTIVE TABLESPACE ATLASPRD3_INDX
);

-- miRBase and Ensembl gene names
DROP TABLE BIOENTITY_NAME;
CREATE TABLE BIOENTITY_NAME (
    identifier VARCHAR(255) NOT NULL,
    organismid NUMBER(22,0) NOT NULL,
    type VARCHAR(50) NOT NULL,
    name VARCHAR(255)
);

CREATE INDEX PK_BIOENTITY_NAME ON BIOENTITY_NAME (IDENTIFIER, ORGANISMID, TYPE) TABLESPACE ATLASPRD3_INDX;
ALTER TABLE BIOENTITY_NAME
  ADD CONSTRAINT PK_BIOENTITY_NAME
  PRIMARY KEY (IDENTIFIER, ORGANISMID, TYPE) USING INDEX PK_BIOENTITY_NAME
ENABLE;

-- miRBase and Ensembl design element to gene mappings
DROP TABLE DESIGNELEMENT_MAPPING;
CREATE TABLE DESIGNELEMENT_MAPPING (
    designelement VARCHAR(255) NOT NULL,
    identifier VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    arraydesign  VARCHAR(255) NOT NULL
);

CREATE INDEX PK_DESIGNELEMENT_MAPPING ON DESIGNELEMENT_MAPPING (DESIGNELEMENT, IDENTIFIER, ARRAYDESIGN) TABLESPACE ATLASPRD3_INDX;
ALTER TABLE DESIGNELEMENT_MAPPING
  ADD CONSTRAINT PK_DESIGNELEMENT_MAPPING
  PRIMARY KEY (DESIGNELEMENT, IDENTIFIER, ARRAYDESIGN) USING INDEX PK_DESIGNELEMENT_MAPPING
ENABLE;

-- Reference table for all bioentity organisms (across Ensembl and miRBase
DROP TABLE BIOENTITY_ORGANISM;
CREATE TABLE BIOENTITY_ORGANISM (
    organismid NUMBER(22,0) NOT NULL,
    name VARCHAR(255) NOT NULL
);

CREATE INDEX PK_BIOENTITY_ORGANISM ON BIOENTITY_ORGANISM (ORGANISMID) TABLESPACE ATLASPRD3_INDX;
ALTER TABLE BIOENTITY_ORGANISM
  ADD CONSTRAINT PK_BIOENTITY_ORGANISM
  PRIMARY KEY (ORGANISMID) USING INDEX PK_BIOENTITY_ORGANISM
ENABLE;

-- Transcript expressions per gene for RNA-seq baseline experiments
DROP TABLE RNASEQ_BSLN_TRANSCRIPTS;
CREATE TABLE RNASEQ_BSLN_TRANSCRIPTS(
    EXPERIMENT VARCHAR(255) NOT NULL
  , GENE_IDENTIFIER VARCHAR(255) NOT NULL
  , TRANSCRIPT_IDENTIFIER VARCHAR(255) NOT NULL
  , TRANSCRIPT_EXPRESSIONS VARCHAR(1000) NOT NULL
  , ISACTIVE VARCHAR2(1) DEFAULT 'T'
)
PARTITION BY LIST (ISACTIVE)
( PARTITION RNASEQ_BSLN_TSCRPT_ACTIVE VALUES ('T')
, PARTITION RNASEQ_BSLN_TSCRPT_INACTIVE VALUES ('F')
 )
PARALLEL;

CREATE INDEX PK_RNASEQ_BSLN_TRANSCRIPTS ON RNASEQ_BSLN_TRANSCRIPTS (EXPERIMENT, GENE_IDENTIFIER, TRANSCRIPT_IDENTIFIER, ISACTIVE) TABLESPACE ATLASPRD3_INDX;
ALTER TABLE RNASEQ_BSLN_TRANSCRIPTS
  ADD CONSTRAINT PK_RNASEQ_BSLN_TRANSCRIPTS
  PRIMARY KEY (EXPERIMENT, GENE_IDENTIFIER, TRANSCRIPT_IDENTIFIER, ISACTIVE) USING INDEX PK_RNASEQ_BSLN_TRANSCRIPTS
ENABLE;

-- Experiment table
DROP TABLE EXPERIMENT;
CREATE TABLE EXPERIMENT(
    ACCESSION VARCHAR(255) NOT NULL,
    TYPE VARCHAR(50) NOT NULL,
    ACCESS_KEY CHAR(36) NOT NULL,
    PRIVATE VARCHAR2(1) DEFAULT 'T',
    LAST_UPDATE DATE DEFAULT sysdate
);

CREATE INDEX PK_EXPERIMENT ON EXPERIMENT (ACCESSION) TABLESPACE ATLASPRD3_INDX;
ALTER TABLE EXPERIMENT
  ADD CONSTRAINT PK_EXPERIMENT
  PRIMARY KEY (ACCESSION) USING INDEX PK_EXPERIMENT
ENABLE;

CREATE OR REPLACE VIEW PUBLIC_EXPERIMENT AS
SELECT ACCESSION, type, LAST_UPDATE
FROM EXPERIMENT WHERE PRIVATE='F';

-- Experiment loading process flag
DROP TABLE EXPERIMENT_LOADING;
CREATE TABLE EXPERIMENT_LOADING(
    INPROGRESS DATE DEFAULT sysdate
);

exit;

