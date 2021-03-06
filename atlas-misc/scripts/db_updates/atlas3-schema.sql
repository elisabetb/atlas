-- Microarray differential analytics
DROP TABLE MICROARRAY_DIFF_ANALYTICS;
CREATE TABLE MICROARRAY_DIFF_ANALYTICS (
    DESIGNELEMENT VARCHAR2(255) NOT NULL
  , EXPERIMENT VARCHAR2(255) NOT NULL
  , ARRAYDESIGN  VARCHAR(255) NOT NULL
  , CONTRASTID VARCHAR2(255) NOT NULL
  , PVAL FLOAT(125) NOT NULL
  , LOG2FOLD FLOAT(125)
  , TSTAT FLOAT(125) 
)
PARALLEL;

CREATE INDEX PK_MA_DIFF_ANALYTICS ON MICROARRAY_DIFF_ANALYTICS (DESIGNELEMENT, EXPERIMENT, CONTRASTID, PVAL) TABLESPACE ATLASPRD3_INDX;
ALTER TABLE MICROARRAY_DIFF_ANALYTICS
  ADD CONSTRAINT PK_MA_DIFF_ANALYTICS
  PRIMARY KEY (DESIGNELEMENT, EXPERIMENT, CONTRASTID, PVAL) USING INDEX PK_MA_DIFF_ANALYTICS
ENABLE;

CREATE INDEX MA_DIFF_ANALYTICS_IDX ON MICROARRAY_DIFF_ANALYTICS (EXPERIMENT, CONTRASTID) TABLESPACE ATLASPRD3_INDX;

-- RNA-seq differential analytics
DROP TABLE RNASEQ_DIFF_ANALYTICS;
CREATE TABLE RNASEQ_DIFF_ANALYTICS (
    IDENTIFIER VARCHAR2(255) NOT NULL
  , EXPERIMENT VARCHAR2(255) NOT NULL
  , CONTRASTID VARCHAR2(255) NOT NULL
  , PVAL FLOAT(125) NOT NULL
  , LOG2FOLD FLOAT(125) 
)
PARALLEL;

CREATE INDEX PK_RNASEQ_DIFF_ANALYTICS ON RNASEQ_DIFF_ANALYTICS (IDENTIFIER, EXPERIMENT, CONTRASTID, PVAL) TABLESPACE ATLASPRD3_INDX;
ALTER TABLE RNASEQ_DIFF_ANALYTICS
  ADD CONSTRAINT PK_RNASEQ_DIFF_ANALYTICS
  PRIMARY KEY (IDENTIFIER, EXPERIMENT, CONTRASTID, PVAL) USING INDEX PK_RNASEQ_DIFF_ANALYTICS
ENABLE;

CREATE INDEX RNASEQ_DIFF_ANALYTICS_IDX ON RNASEQ_DIFF_ANALYTICS (EXPERIMENT, CONTRASTID) TABLESPACE ATLASPRD3_INDX;


-- Differential analytics across all types of experiments, with additional gene name and organism information, restricted to FDR<0.05 and log2fold>=1
-- Used by the UI
DROP MATERIALIZED VIEW VW_DIFFANALYTICS;
CREATE MATERIALIZED VIEW VW_DIFFANALYTICS
NOLOGGING PARALLEL 16
BUILD DEFERRED
REFRESH COMPLETE ON DEMAND
ENABLE QUERY REWRITE AS
select IDENTIFIER, NAME, ORGANISM, EXPERIMENT, CONTRASTID, PVAL, LOG2FOLD, TSTAT
from (
select dem.IDENTIFIER, bn.NAME AS NAME, o.NAME AS ORGANISM, mda.EXPERIMENT, mda.CONTRASTID, mda.PVAL, mda.LOG2FOLD, mda.TSTAT
,rank() over(partition by mda.EXPERIMENT, mda.CONTRASTID, dem.IDENTIFIER order by(abs(mda.LOG2FOLD)) desc) as lfrank
from MICROARRAY_DIFF_ANALYTICS mda
join DESIGNELEMENT_MAPPING dem on mda.designelement=dem.designelement and mda.arraydesign = dem.arraydesign
join BIOENTITY_NAME bn on dem.identifier=bn.identifier
join BIOENTITY_ORGANISM o on bn.organismid = o.organismid
where abs(mda.LOG2FOLD) >= 1
and mda.PVAL < 0.05
) where lfrank = 1
union all
select rda.IDENTIFIER, bn.NAME AS NAME, o.name AS ORGANISM, rda.EXPERIMENT, rda.CONTRASTID, rda.PVAL, rda.LOG2FOLD, null
from RNASEQ_DIFF_ANALYTICS rda
join BIOENTITY_NAME bn on rda.IDENTIFIER=bn.identifier
join BIOENTITY_ORGANISM o on bn.organismid = o.organismid
join EXPERIMENT_ORGANISM eo on o.name = eo.bioentity_organism and eo.experiment = rda.experiment
where abs(rda.LOG2FOLD) >= 1
and rda.PVAL < 0.05;

exec dbms_mview.refresh( 'VW_DIFFANALYTICS', 'C' );

-- Differential analytics across all types of experiments, with additional gene name and organism information, restricted to FDR<0.05 only
-- Used to generate a data dump at Atlas release time
DROP MATERIALIZED VIEW VW_DIFFANALYTICS_DUMP;
CREATE MATERIALIZED VIEW VW_DIFFANALYTICS_DUMP
NOLOGGING PARALLEL 16
BUILD DEFERRED
REFRESH COMPLETE ON DEMAND
ENABLE QUERY REWRITE AS
select IDENTIFIER, NAME, ORGANISM, EXPERIMENT, CONTRASTID, PVAL, LOG2FOLD, TSTAT
from (
select dem.IDENTIFIER, bn.NAME AS NAME, o.NAME AS ORGANISM, mda.EXPERIMENT, mda.CONTRASTID, mda.PVAL, mda.LOG2FOLD, mda.TSTAT
,rank() over(partition by mda.EXPERIMENT, mda.CONTRASTID, dem.IDENTIFIER order by(abs(mda.LOG2FOLD)) desc) as lfrank
from MICROARRAY_DIFF_ANALYTICS mda
join DESIGNELEMENT_MAPPING dem on mda.designelement=dem.designelement and mda.arraydesign = dem.arraydesign
join BIOENTITY_NAME bn on dem.identifier=bn.identifier
join BIOENTITY_ORGANISM o on bn.organismid = o.organismid
and mda.PVAL < 0.05
) where lfrank = 1
union all
select rda.IDENTIFIER, bn.NAME AS NAME, o.name AS ORGANISM, rda.EXPERIMENT, rda.CONTRASTID, rda.PVAL, rda.LOG2FOLD, null
from RNASEQ_DIFF_ANALYTICS rda
join BIOENTITY_NAME bn on rda.IDENTIFIER=bn.identifier
join BIOENTITY_ORGANISM o on bn.organismid = o.organismid
join EXPERIMENT_ORGANISM eo on o.name = eo.bioentity_organism and eo.experiment = rda.experiment
and rda.PVAL < 0.05;

exec dbms_mview.refresh( 'VW_DIFFANALYTICS_DUMP', 'C' );


-- RNA-seq baseline analytics
DROP TABLE RNASEQ_BSLN_EXPRESSIONS;
CREATE TABLE RNASEQ_BSLN_EXPRESSIONS (
    IDENTIFIER VARCHAR2(255) NOT NULL
  , EXPERIMENT VARCHAR2(255) NOT NULL
  , ASSAYGROUPID VARCHAR2(255) NOT NULL
  , EXPRESSION FLOAT(125) NOT NULL
)
PARALLEL;

CREATE INDEX PK_RSQ_BSLN_ANALYTICS ON RNASEQ_BSLN_EXPRESSIONS (IDENTIFIER, EXPERIMENT, ASSAYGROUPID, EXPRESSION) TABLESPACE ATLASPRD3_INDX;
ALTER TABLE RNASEQ_BSLN_EXPRESSIONS
  ADD CONSTRAINT PK_RSQ_BSLN_EXPRESSIONS
  PRIMARY KEY (IDENTIFIER, EXPERIMENT, ASSAYGROUPID, EXPRESSION) USING INDEX PK_RSQ_BSLN_ANALYTICS
ENABLE;

CREATE INDEX RSQ_BSLN_EXPRESSIONS_IDX ON RNASEQ_BSLN_EXPRESSIONS (IDENTIFIER, EXPERIMENT, ASSAYGROUPID) TABLESPACE ATLASPRD3_INDX;


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

-- Reference table mapping array design accession in DESIGNELEMENT_MAPPING to their user-friendly names
DROP TABLE ARRAYDESIGN;
CREATE TABLE ARRAYDESIGN (
    accession  VARCHAR(255) NOT NULL,
    name  VARCHAR(255) NOT NULL
);

CREATE INDEX PK_ARRAYDESIGN ON ARRAYDESIGN (ACCESSION) TABLESPACE ATLASPRD3_INDX;
ALTER TABLE ARRAYDESIGN
  ADD CONSTRAINT PK_ARRAYDESIGN
  PRIMARY KEY (ACCESSION) USING INDEX PK_ARRAYDESIGN
ENABLE;

ALTER TABLE DESIGNELEMENT_MAPPING
  ADD CONSTRAINT FK_ARRAYDESIGN FOREIGN KEY (ARRAYDESIGN) REFERENCES arraydesign(ACCESSION) ON DELETE CASCADE
ENABLE;

ALTER TABLE MICROARRAY_DIFF_ANALYTICS
  ADD CONSTRAINT FK_ARRAYDESIGN FOREIGN KEY (ARRAYDESIGN) REFERENCES arraydesign(ACCESSION) ON DELETE CASCADE
ENABLE;

-- Reference table for all bioentity organisms (across Ensembl and miRBase)
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

-- Experiment table
ALTER TABLE EXPERIMENT_ORGANISM DROP CONSTRAINT FK_EXPERIMENT_ORGANISM;
DROP TABLE EXPERIMENT;
CREATE TABLE EXPERIMENT(
    ACCESSION VARCHAR(255) NOT NULL,
    TYPE VARCHAR(50) NOT NULL,
    ACCESS_KEY CHAR(36) NOT NULL,
    PRIVATE VARCHAR2(1) DEFAULT 'T',
    LAST_UPDATE DATE DEFAULT sysdate,
    PUBMED_IDS VARCHAR2(255),
    TITLE VARCHAR2(500)
);

CREATE INDEX PK_EXPERIMENT ON EXPERIMENT (ACCESSION) TABLESPACE ATLASPRD3_INDX;
ALTER TABLE EXPERIMENT
  ADD CONSTRAINT PK_EXPERIMENT
  PRIMARY KEY (ACCESSION) USING INDEX PK_EXPERIMENT
ENABLE;

CREATE OR REPLACE VIEW PUBLIC_EXPERIMENT AS
SELECT ACCESSION, type, LAST_UPDATE
FROM EXPERIMENT WHERE PRIVATE='F';

-- Species table
DROP TABLE EXPERIMENT_ORGANISM;
CREATE TABLE EXPERIMENT_ORGANISM(
    ORGANISM VARCHAR(255) NOT NULL,
    EXPERIMENT VARCHAR(255) NOT NULL,
    BIOENTITY_ORGANISM AS (NVL(substr(ORGANISM,0,instr(ORGANISM,' ',1,2) - 1),ORGANISM))
);

CREATE INDEX ES_EXPERIMENT_ACCESSION ON EXPERIMENT_ORGANISM (EXPERIMENT) TABLESPACE ATLASPRD3_INDX;
ALTER TABLE EXPERIMENT_ORGANISM
  ADD CONSTRAINT FK_EXPERIMENT_ORGANISM FOREIGN KEY (EXPERIMENT) REFERENCES EXPERIMENT(ACCESSION) ON DELETE CASCADE
ENABLE;

-- Table for storing sample attribute-value mappings to ontology
DROP TABLE ONTOLOGY_MAPPINGS;
CREATE TABLE ONTOLOGY_MAPPINGS (
    PROPERTY_NAME VARCHAR(255) NOT NULL,
    PROPERTY_VALUE VARCHAR(255) NOT NULL,
    ONTOLOGY_URI VARCHAR(255) NOT NULL,
    DATE_UPDATE TIMESTAMP NOT NULL
);

CREATE INDEX PK_ONTOLOGY_MAPPINGS ON ONTOLOGY_MAPPINGS (PROPERTY_NAME, PROPERTY_VALUE) TABLESPACE ATLASPRD3_INDX;
ALTER TABLE ONTOLOGY_MAPPINGS
  ADD CONSTRAINT PK_ONTOLOGY_MAPPINGS
  PRIMARY KEY (PROPERTY_NAME, PROPERTY_VALUE) USING INDEX PK_ONTOLOGY_MAPPINGS
ENABLE;

-- Loading table for ONTOLOGY_MAPPINGS
DROP TABLE ONTOLOGY_MAPPINGS_LOADING;
CREATE TABLE ONTOLOGY_MAPPINGS_LOADING (
    PROPERTY_NAME VARCHAR(255) NOT NULL,
    PROPERTY_VALUE VARCHAR(255) NOT NULL,
    ONTOLOGY_URI VARCHAR(255) NOT NULL
);

-- Table for storing co-expressed genes in baseline experiments
DROP TABLE RNASEQ_BSLN_CE_PROFILES;
CREATE TABLE RNASEQ_BSLN_CE_PROFILES (
  EXPERIMENT VARCHAR2(255) NOT NULL,
  IDENTIFIER VARCHAR(255) NOT NULL,
  CE_IDENTIFIERS CLOB NOT NULL
);

CREATE INDEX PK_RNASEQ_BSLN_CE_PROFILES ON RNASEQ_BSLN_CE_PROFILES (EXPERIMENT, IDENTIFIER) TABLESPACE ATLASPRD3_INDX;
ALTER TABLE RNASEQ_BSLN_CE_PROFILES
    ADD CONSTRAINT PK_RNASEQ_BSLN_CE_PROFILES
    PRIMARY KEY (EXPERIMENT, IDENTIFIER) USING INDEX PK_RNASEQ_BSLN_CE_PROFILES
ENABLE;

-- Data processing in-progress flag table - used for data production in Atlas - no dependency on it exists in the Web services code
DROP TABLE ATLAS_JOBS;
CREATE TABLE ATLAS_JOBS(
    DATE_STARTED DATE DEFAULT sysdate,
    JOBTYPE VARCHAR(255) NOT NULL,
    JOBOBJECT VARCHAR(255) NULL -- Experiment accession or organism
);

exit;
