-- ============================================================
-- SQL Script: Create all tables for Manufacturing Lab Microsite
-- Database: SQL Server
-- Schema: dbo
-- ============================================================
--DATABASE CREATION
create database ixmicrosite

--TO USE CREATED DATABASE
use ixmicrosite

--TO CREATE SCHEMA
create schema mfg

-- 1. Industry
CREATE TABLE mfg.industry (
    industry_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
    industry_name VARCHAR(255) NOT NULL
);

-- 2. Sub-Industry
CREATE TABLE mfg.sub_industry (
    sub_industry_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
    sub_industry_name VARCHAR(255) NOT NULL,
    industry_id       BIGINT       NOT NULL,
    CONSTRAINT FK_sub_industry_industry FOREIGN KEY (industry_id) REFERENCES dbo.industry (industry_id)
);

-- 3. Value Chain
CREATE TABLE mfg.value_chain (
    value_chain_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
    value_chain_name VARCHAR(255) NOT NULL,
    industry_id      BIGINT       NOT NULL,
    sub_industry_id  BIGINT       NOT NULL,
    CONSTRAINT FK_value_chain_industry     FOREIGN KEY (industry_id)     REFERENCES dbo.industry (industry_id),
    CONSTRAINT FK_value_chain_sub_industry FOREIGN KEY (sub_industry_id) REFERENCES dbo.sub_industry (sub_industry_id)
);

-- 4. User Management
CREATE TABLE mfg.user_management (
    user_id       INT IDENTITY(1,1) PRIMARY KEY,
    user_eid      VARCHAR(100) NOT NULL,
    is_presenter  BIT          NOT NULL,
    is_admin      BIT          NOT NULL,
    is_superadmin BIT          NOT NULL,
    is_active     BIT          NOT NULL
);

-- 5. Use Case (identity seed starts at 10000)
CREATE TABLE mfg.usecase (
    usecase_id         INT IDENTITY(10000,1) PRIMARY KEY,
    value_chain_id     BIGINT          NOT NULL,
    creator_id         INT          NOT NULL,
    title              VARCHAR(255) NOT NULL,
    owner_eid          VARCHAR(100) NOT NULL,
    status             VARCHAR(20)  NOT NULL,
    approver_id        INT          NULL,
    approved_date      DATETIME     NULL,
    parent_usecase_id  INT          NULL,
    is_updated_usecase BIT          NOT NULL,
    created_date       DATETIME     NOT NULL,
    updated_date       DATETIME     NULL,
    is_active          BIT          NOT NULL,
    CONSTRAINT FK_usecase_value_chain FOREIGN KEY (value_chain_id) REFERENCES dbo.value_chain (value_chain_id),
    CONSTRAINT FK_usecase_creator     FOREIGN KEY (creator_id)     REFERENCES dbo.user_management (user_id)
);

-- 6. Use Case Speakers
CREATE TABLE mfg.usecase_speakers (
    speaker_id   INT IDENTITY(1,1) PRIMARY KEY,
    usecase_id   INT          NOT NULL,
    speaker_eid  VARCHAR(100) NOT NULL,
    speaker_type VARCHAR(20)  NOT NULL,
    CONSTRAINT FK_usecase_speakers_usecase FOREIGN KEY (usecase_id) REFERENCES dbo.usecase (usecase_id)
);

-- 7. Use Case Tags
CREATE TABLE mfg.usecase_tags (
    tag_id     INT IDENTITY(1,1) PRIMARY KEY,
    usecase_id INT          NOT NULL,
    tag        VARCHAR(100) NOT NULL,
    CONSTRAINT FK_usecase_tags_usecase FOREIGN KEY (usecase_id) REFERENCES dbo.usecase (usecase_id)
);

-- 8. Use Case Artifacts
CREATE TABLE mfg.usecase_artifacts (
    artifact_id   INT IDENTITY(1,1) PRIMARY KEY,
    usecase_id    INT          NOT NULL,
    artifact_type VARCHAR(30)  NOT NULL,
    url           VARCHAR(500) NOT NULL,
    CONSTRAINT FK_usecase_artifacts_usecase FOREIGN KEY (usecase_id) REFERENCES dbo.usecase (usecase_id)
);

-- 9. Use Case Content
CREATE TABLE mfg.usecase_content (
    usecase_content_id INT IDENTITY(1,1) PRIMARY KEY,
    usecase_id         INT          NOT NULL,
    description        NVARCHAR(MAX) NULL,
    business_problem   NVARCHAR(MAX) NULL,
    solution           NVARCHAR(MAX) NULL,
    tools_and_technologies NVARCHAR(MAX) NULL,
    key_results        NVARCHAR(MAX) NULL,
    value_delivered    NVARCHAR(MAX) NULL,
    duration           INT           NULL,
    thumbnail_url      VARCHAR(500)  NULL,
    CONSTRAINT FK_usecase_content_usecase FOREIGN KEY (usecase_id) REFERENCES dbo.usecase (usecase_id)
);

-- /*insert query for industry table*/

INSERT INTO mfg.industry (industry_name) VALUES ('Consumer Package Goods'), ('Life Sciences'),
 ('Energy'), ('Industrials'), ('Utilities'), ('Chemicals and Natural Services'), ('High Tech');

-- /*insert query for sub_industry table */

INSERT INTO mfg.sub_industry (sub_industry_name, industry_id)

VALUES ('Pharmaceuticals', 2);

--/*insert query for valu_chain table */
INSERT INTO mfg.value_chain (value_chain_name, sub_industry_id, industry_id) VALUES

('Clinical Trails', 1, 2),

('R&D', 1, 2),

('Raw/Package Material Warehouse', 1, 2),

('Manufacturing', 1, 2),

('Packaging', 1, 2),

('Quality Management System', 1, 2),

('Finished Goods Warehouse', 1, 2);

-- /*insert query for user_management table */

INSERT INTO mfg.user_management
(user_eid, is_presenter, is_admin, is_superadmin, is_active)
VALUES
('gudluru.yashwanth',      1, 0, 0, 1),
('shashi.veeramalla',      0, 1, 0, 1),
('mukunda.ram.bhuyan',     0, 0, 1, 1),
('prashanthi.guduru',      1, 1, 0, 1),
('sharavanth.r.s',         0, 0, 0, 1),
('dali.sowjanya.alla',     1, 0, 1, 1),
('sheshu.gandhasiri',      0, 1, 1, 1),
('mainak.kumar.maiti',     1, 1, 1, 1);


--Select Queries
select * from mfg.industry

select * from mfg.sub_industry

select * from mfg.value_chain

select * from mfg.usecase

select * from mfg.usecase_content

select * from mfg.usecase_tags

select * from mfg.usecase_speakers

select * from mfg.user_management

select * from usecase_artifacts

