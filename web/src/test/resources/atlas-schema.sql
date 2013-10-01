CREATE TABLE IF NOT EXISTS RNASEQ_BSLN_TRANSCRIPTS(
    EXPERIMENT VARCHAR(255) NOT NULL,
    GENE_IDENTIFIER VARCHAR(255) NOT NULL,
    TRANSCRIPT_IDENTIFIER VARCHAR(255) NOT NULL,
    TRANSCRIPT_EXPRESSIONS ARRAY NOT NULL,
    ISACTIVE BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (EXPERIMENT, GENE_IDENTIFIER, TRANSCRIPT_IDENTIFIER)
);

CREATE TABLE IF NOT EXISTS EXPERIMENT(
    ACCESSION VARCHAR(255) NOT NULL,
    TYPE VARCHAR(50) NOT NULL,
    PRIVATE VARCHAR(1) NOT NULL,
    LAST_UPDATE TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    ACCESS_KEY VARCHAR(50),
    TITLE VARCHAR(255),
    PUBMED_IDS VARCHAR(255),
    PRIMARY KEY (ACCESSION)
);

CREATE OR REPLACE VIEW PUBLIC_EXPERIMENT AS
SELECT ACCESSION, TYPE, LAST_UPDATE
FROM EXPERIMENT WHERE PRIVATE='F';

CREATE TABLE IF NOT EXISTS bioentity_name(
    identifier VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    organism  VARCHAR(255),
    type VARCHAR(50),
    PRIMARY KEY (identifier, organism, type)
);

CREATE TABLE IF NOT EXISTS designelement_mapping(
    designelement VARCHAR(255) NOT NULL,
    identifier VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    arraydesign  VARCHAR(255),
    PRIMARY KEY (designelement, arraydesign)
);

CREATE TABLE IF NOT EXISTS EXPERIMENT_SPECIES(
    SPECIES VARCHAR(255) NOT NULL,
    EXPERIMENT VARCHAR(255) NOT NULL,
    FOREIGN KEY (EXPERIMENT) REFERENCES EXPERIMENT(ACCESSION) ON DELETE CASCADE
);
-- enable the next statement only after successful migration
DROP TABLE IF EXISTS experiment_configuration;