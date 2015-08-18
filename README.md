# WebReport
## A prototype report system for dcm4chee 2. 
I started this project for a freelance job that has gone awry. Feel free to use it anyway you like. 

It builds an EAR file that is supposed to be deployed together with the dcm4chee PACS. It's back end is a JavaEE app, using Hibernate and stateless EJBs providing REST endpoints. The front end is an Angular app using CKEditor.

Current features:
* querying studies on dcm4chee,
* selecting (and locking) a study for reporting
** locking only affects this app (leaves the PACS alone), and prevents concurrent reporting of a study   
* creating a report for the selected study,
* adding amendments to a report
* user inactivity monitor
** releases study lock and tries to re-acquire it on resume

### Note
The provided database script was written for PostgreSQL. It should be trivial to adapt it to any other database.

## TODO
* Deliver reports (stop creation of new amendments)
* User login
** associate user to report
** user's worklist (queue)
* Report printing
* Report templates
* Audio (report dictation and playback) 
* Review error control