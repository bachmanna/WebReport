package com.bisol.medical.webreport.persistence;

import java.util.Date;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
@Local(ReportDao.class)
public class ReportDaoBean implements ReportDao {
	
	@PersistenceContext(unitName="pacsPersistence")
	private EntityManager em;
	
	@Override
	public Report findByStudy(long studyPk){
		List<?> result = em.createNamedQuery("webreport.byStudy").setParameter("studyPk", studyPk).getResultList();
		return (Report) (result == null || result.isEmpty() ? null : result.get(0)); // 1 or 0, damn getSingleResult for not supporting empty result 
	}

	@Override
	public Report create(long studyPk, String report, Date reportDatetime) {
		Report entity = new Report(report, reportDatetime, studyPk);
		em.persist(entity);
		return entity;
	}

	@Override
	public ReportAmendment createAmendment(long reportPk, String report, Date reportDatetime) {
		ReportAmendment entity = new ReportAmendment(reportPk, report, reportDatetime);
		em.persist(entity);
		return entity;
	}

	@Override
	public boolean exists(long pk) {
		List<?> res = em.createNamedQuery("webreport.exists").setParameter("pk", pk).getResultList();
		return res != null && !res.isEmpty();
	}

	@Override
	public boolean isReported(long studyPk) {
		List<?> res = em.createNamedQuery("webreport.isReported").setParameter("studyPk", studyPk).getResultList();
		return res != null && !res.isEmpty();
	}

	@Override
	public Report findById(long reportPk) {
		return em.find(Report.class, reportPk);
	}

	@Override
	public ReportAmendment findReportAmendmentById(long amendmentPk) {
		return em.find(ReportAmendment.class, amendmentPk);
	}
}