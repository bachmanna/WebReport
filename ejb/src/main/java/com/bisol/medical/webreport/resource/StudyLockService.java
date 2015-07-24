package com.bisol.medical.webreport.resource;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.bisol.medical.webreport.persistence.StudyDto;

public class StudyLockService {
	private static final Map<Long, StudyLock> studyLocks = new HashMap<>();
	private static final Timer reaper = new Timer(StudyLockService.class.getSimpleName() + "Reaper");
	private static final Logger logger = Logger.getLogger(StudyLockService.class);
	
	public static boolean isLocked(StudyDto study){
		return studyLocks.get(study.pk) != null;
	}
	
	public static void unlockStudy(long studyPk, UUID lockId){
		synchronized(studyLocks){
			StudyLock currentLock = studyLocks.get(studyPk);
			if(currentLock == null){
				logger.info("Study already unlocked");	// debug
			}
			
			if(currentLock.getId().equals(lockId)){
				studyLocks.remove(studyPk);
				logger.info("Unlocked study " + studyPk);// debug
			} else {
				throw new IllegalStateException("Cannot unlock study: study " + studyPk + " has a new lock!");
			}
			
		}
	}
	
	public static void renewLock(long studyPk, UUID lockId, long howLong) {
		synchronized(studyLocks){
			StudyLock currentLock = studyLocks.get(studyPk);
			if(currentLock == null){
				throw new IllegalStateException("Study already unlocked");
			}
			
			if(currentLock.getId().equals(lockId)){
				long expireTimestamp = new Date().getTime() + howLong;
				currentLock.setExpireTimestamp(expireTimestamp);
				logger.info("Renewed lock for study " + studyPk);// debug
			} else {
				throw new IllegalStateException("Study has a new lock: " + currentLock.getId().toString() + ", recv = " + lockId.toString());
			}
		}
	}
	
	public static StudyLock lockStudy(long studyPk, long howLong){
		synchronized(studyLocks){
			StudyLock currentLock = studyLocks.get(studyPk);
			if(currentLock != null){
				throw new IllegalStateException("Study already locked by '" + currentLock + "'");
			}
			
			long expireTimestamp = new Date().getTime() + howLong;
			StudyLock newLock = new StudyLock(studyPk, expireTimestamp);
			studyLocks.put(studyPk, newLock);
			reaper.schedule(new ReaperTask(newLock), howLong);
			
			logger.info("Locked study " + studyPk);
			return newLock;
		}
	}
	
	private static class ReaperTask extends TimerTask {
		private final StudyLock myLock;
		
		public ReaperTask(StudyLock studyLock) {
			this.myLock = studyLock;
		}

		@Override
		public void run() {
			long studyKey = myLock.getEntityPk();
			synchronized (studyLocks) {
				StudyLock currentLock = studyLocks.get(studyKey);
				if(currentLock != null){
					if(currentLock.getId().equals(myLock.getId())){
						long currentTimestamp = new Date().getTime();
						if(currentLock.getExpireTimestamp() <= currentTimestamp){
							studyLocks.remove(studyKey);
							logger.info("Reaper: Unlocked study " + studyKey);// debug
						} else {
							reaper.schedule(new ReaperTask(myLock), myLock.getExpireTimestamp() - currentTimestamp);
							logger.info("Reaper: lock renewed, reescheduling reaper for study " + studyKey);// debug
						}
					} else {
						logger.warn("Reaper: Cannot unlock study: study " + studyKey + " has a new lock!");	//error?					
					}
				} else {
					logger.info("Reaper: Cannot unlock study: study " + studyKey + " has no locks!"); //debug						
				}
			}
		}
	}
}

class StudyLock {
	private final long entityPk;
	private final String userName = "testUser"; //TODO add suport for loged users
	private final UUID id = UUID.randomUUID();
	private long expireTimestamp;
	
	public StudyLock(long entityPk, long expireTimestamp) {
		this.entityPk = entityPk;
		this.expireTimestamp = expireTimestamp;
	}

	public void setExpireTimestamp(long newTimestamp) {
		this.expireTimestamp = newTimestamp;
	}
	
	public long getExpireTimestamp() {
		return expireTimestamp;
	}

	public String getUserName() {
		return userName;
	}

	public long getEntityPk() {
		return entityPk;
	}

	public UUID getId() {
		return id;
	}
}