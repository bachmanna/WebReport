CREATE TABLE webreport (
	pk				SERIAL8 NOT NULL CONSTRAINT webreport_pk PRIMARY KEY,
	study_fk		SERIAL8 UNIQUE NOT NULL,
	report_data		TEXT NOT NULL,
	report_datetime	TIMESTAMP NOT NULL,
	status			VARCHAR(15) NOT NULL DEFAULT 'typed',
	CONSTRAINT webreport_study FOREIGN KEY (study_fk) REFERENCES study(pk)
);

CREATE TABLE webreport_amendment (
	pk					SERIAL8 NOT NULL CONSTRAINT webreport_amendment_pk PRIMARY KEY,
	webreport_fk		SERIAL8 NOT NULL,
	report_data			TEXT NOT NULL,
	amendment_datetime	TIMESTAMP NOT NULL,
	status				VARCHAR(15) NOT NULL DEFAULT 'typed',
	CONSTRAINT amendment_webreport FOREIGN KEY (webreport_fk) REFERENCES webreport(pk)
);

CREATE INDEX webreport_study_fk ON webreport(study_fk);
CREATE INDEX webreport_amendment_fk ON webreport_amendment(webreport_fk);

--drop table webreport_amendment;
--drop table webreport;

SELECT *FROM webreport report WHERE report.study_fk = 1;