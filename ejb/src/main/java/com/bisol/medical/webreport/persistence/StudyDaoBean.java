package com.bisol.medical.webreport.persistence;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.bisol.medical.webreport.ReportStatus;

@Stateless
@Local(StudyDao.class)
public class StudyDaoBean implements StudyDao {
	
	@PersistenceContext(unitName="pacsPersistence")
	private EntityManager em;
	
	private static final String jdbcQuery = 
					"SELECT patient.pat_id, patient.pat_name, study.accession_no, study.study_datetime, study.mods_in_study, study.study_iuid, study.pk, report.pk, report.status "
					+ " FROM patient patient JOIN study study ON (study.patient_fk = patient.pk) LEFT JOIN webreport report ON (study.pk = report.study_fk) ";
	
	@Override
	public List<StudyDto> query(int offset, int limit, String patientID, String patientName, String accessionNumber, Date startDate, Date endDate, String modality, ReportStatus reportStatus){
		int paramIndex = 1;
		StringBuilder queryBuilder = new StringBuilder(jdbcQuery);
		
		paramIndex = addStringPredicate(queryBuilder, "patient.pat_id", patientID, paramIndex);
		paramIndex = addStringPredicate(queryBuilder, "patient.pat_name", patientName, paramIndex);
		paramIndex = addStringPredicate(queryBuilder, "study.accession_no", accessionNumber, paramIndex);
		paramIndex = addStringPredicate(queryBuilder, "study.mods_in_study", modality, paramIndex);
		paramIndex = addObjectPredicate(queryBuilder, "report.status", reportStatus, paramIndex);
		// ORDER SENSITIVE (SEE BELOW)
		paramIndex = addTimestampPredicate(queryBuilder, "study.study_datetime", startDate, endDate, paramIndex);

		Query query = em.createNativeQuery(queryBuilder.toString());
		
		addPredicateValue(query, "patient.pat_id", patientID);
		addPredicateValue(query, "patient.pat_name", patientName);
		addPredicateValue(query, "study.accession_no", accessionNumber);
		addPredicateValue(query, "study.mods_in_study", modality);
		addPredicateValue(query, "report.status", reportStatus);
		// ORDER SENSITIVE (SEE ABOVE)
		paramIndex = 1; 
		paramIndex = addPredicateValue(query, startDate, paramIndex);
		paramIndex = addPredicateValue(query, endDate, paramIndex);
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultSet = query.setFirstResult(offset).setMaxResults(limit).getResultList();
		return mapResultSet(resultSet);
	}

	private int addStringPredicate(StringBuilder query, String paramName, String paramValue, int paramIndex){
		if(paramValue != null && !paramValue.isEmpty()){
			addWhereOrAnd(query, paramIndex);
			query.append(paramName).append(" like :").append(paramName);
		}
		return paramIndex;
	}

	private int addObjectPredicate(StringBuilder query, String paramName, Object paramValue, int paramIndex){
		if(paramValue != null){
			addWhereOrAnd(query, paramIndex);
			query.append(paramName).append(" like :").append(paramName);
		}
		return paramIndex;
	}

	private int addTimestampPredicate(StringBuilder query, String paramName, Date startDate, Date endDate, int paramIndex){
		if(startDate != null){
			addWhereOrAnd(query, paramIndex);
			query.append(paramName).append(" >= ?").append(paramIndex++);
		}
		
		if(endDate != null){
			addWhereOrAnd(query, paramIndex);
			query.append(paramName).append(" <= ?").append(paramIndex++);
		}
		
		return paramIndex;
	}

	private void addWhereOrAnd(StringBuilder query, int paramIndex) {
		if(paramIndex > 1){
			query.append(" AND ");
		} else {
			query.append(" WHERE ");
		}
	}
	
	private void addPredicateValue(Query query, String paramName, String paramValue){
		if(paramValue != null && !paramValue.isEmpty()){
			query.setParameter(paramName, paramValue);
		}
	}
	
	private void addPredicateValue(Query query, String paramName, ReportStatus paramValue){
		if(paramValue != null){
			query.setParameter(paramName, paramValue.name());
		}
	}
	
	private int addPredicateValue(Query query, Date paramValue, int paramIndex){
		if(paramValue != null){
			query.setParameter(paramIndex++, paramValue);
		}
		return paramIndex;
	}
	
	private List<StudyDto> mapResultSet(List<Object[]> resultSet){
		List<StudyDto> studies = new ArrayList<StudyDto>(resultSet.size());
		for(Object[] tuple : resultSet){
			String patientId = (String) tuple[0];
			String patientName = (String) tuple[1];
			String accNbr = (String) tuple[2];
			Timestamp studyDateTime = (Timestamp) tuple[3];
			String modalities = (String) tuple[4];
			String instanceUid = (String) tuple[5];
			long pk = ((Number) tuple[6]).longValue();
			long reportPk = ((Number) tuple[7]).longValue();
			String reportStatus = (String) tuple[8];
			
			studies.add(new StudyDto(pk, patientId, patientName, accNbr, studyDateTime, modalities, instanceUid, reportPk, reportStatus));
		}
		
		return studies;
	}
}