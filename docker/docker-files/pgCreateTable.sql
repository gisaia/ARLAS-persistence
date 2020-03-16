DROP TABLE IF EXISTS user_data;
-- key,id,data
CREATE TABLE user_data (
    docKey VARCHAR(255) NOT NULL,
    docType VARCHAR(255) NOT NULL,
    id VARCHAR(255) PRIMARY KEY,
    creationDate TIMESTAMP,
    docValue JSON
);
CREATE INDEX doc_key_idx ON user_data (docKey, docType);