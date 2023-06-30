INSERT INTO DOCTOR_TYPE(DOCTOR_TYPE_ID, DOCTOR_TYPE_CODE, DOCTOR_TYPE_NAME, DOCTOR_TYPE_DURATION_MIN, DOCTOR_TYPE_DURATION_MAX, DOCTOR_TYPE_OVERLAPPING_APPOINTMENTS)
VALUES (101,'1', 'General', 5, 15, 2);
INSERT INTO DOCTOR_TYPE(DOCTOR_TYPE_ID, DOCTOR_TYPE_CODE, DOCTOR_TYPE_NAME, DOCTOR_TYPE_DURATION_MIN, DOCTOR_TYPE_DURATION_MAX, DOCTOR_TYPE_OVERLAPPING_APPOINTMENTS)
VALUES (102, '2', 'Specialist', 10, 30, 3);
INSERT INTO DOCTOR(DOCTOR_ID, DOCTOR_NAME, DOCTOR_TYPE_ID) VALUES (103, 'Mohammad Rezaie', 101);
INSERT INTO DOCTOR(DOCTOR_ID, DOCTOR_NAME, DOCTOR_TYPE_ID) VALUES (104, 'Mehrnoosh Vafaie', 102);
INSERT INTO DOCTOR(DOCTOR_ID, DOCTOR_NAME, DOCTOR_TYPE_ID) VALUES (105, 'Kian Ghanbari', 101);
INSERT INTO DOCTOR(DOCTOR_ID, DOCTOR_NAME, DOCTOR_TYPE_ID) VALUES (106, 'Mozhdeh Mozhdegani', 101);
INSERT INTO PATIENT(PATIENT_ID, PATIENT_NAME) VALUES (107, 'Karim Saghir');
INSERT INTO PATIENT(PATIENT_ID, PATIENT_NAME) VALUES (108, 'Matin Nazif');
INSERT INTO PATIENT(PATIENT_ID, PATIENT_NAME) VALUES (109, 'Rasa Koosha');
INSERT INTO PATIENT(PATIENT_ID, PATIENT_NAME) VALUES (110, 'Homa Pooya');
INSERT INTO PATIENT(PATIENT_ID, PATIENT_NAME) VALUES (111, 'Ali Khakzad');
INSERT INTO PATIENT(PATIENT_ID, PATIENT_NAME) VALUES (112, 'Shima Shayan');
INSERT INTO PATIENT(PATIENT_ID, PATIENT_NAME) VALUES (113, 'Nima Noori');
INSERT INTO PATIENT(PATIENT_ID, PATIENT_NAME) VALUES (114, 'Saba Sana');
INSERT INTO PATIENT(PATIENT_ID, PATIENT_NAME) VALUES (115, 'Negar Yari');
INSERT INTO SCHEDULE_DETAIL(SCHEDULE_DETAIL_ID, SCHEDULE_DATE_TIME_START, SCHEDULE_DATE_TIME_END) VALUES (116, TO_TIMESTAMP('2023-07-01 09:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2023-07-01 11:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO SCHEDULE_DETAIL(SCHEDULE_DETAIL_ID, SCHEDULE_DATE_TIME_START, SCHEDULE_DATE_TIME_END) VALUES (117, TO_TIMESTAMP('2023-07-01 10:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2023-07-01 12:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO SCHEDULE_DETAIL(SCHEDULE_DETAIL_ID, SCHEDULE_DATE_TIME_START, SCHEDULE_DATE_TIME_END) VALUES (118, TO_TIMESTAMP('2023-07-01 16:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2023-07-01 18:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO SCHEDULE_DETAIL(SCHEDULE_DETAIL_ID, SCHEDULE_DATE_TIME_START, SCHEDULE_DATE_TIME_END) VALUES (119, TO_TIMESTAMP('2023-07-02 11:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2023-07-01 13:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO SCHEDULE_DETAIL(SCHEDULE_DETAIL_ID, SCHEDULE_DATE_TIME_START, SCHEDULE_DATE_TIME_END) VALUES (120, TO_TIMESTAMP('2023-07-02 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2023-07-01 17:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO SCHEDULE_DETAIL(SCHEDULE_DETAIL_ID, SCHEDULE_DATE_TIME_START, SCHEDULE_DATE_TIME_END) VALUES (121, TO_TIMESTAMP('2023-07-03 15:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2023-07-01 17:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO SCHEDULE_DETAIL(SCHEDULE_DETAIL_ID, SCHEDULE_DATE_TIME_START, SCHEDULE_DATE_TIME_END) VALUES (122, TO_TIMESTAMP('2023-07-03 14:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2023-07-03 16:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO SCHEDULE(SCHEDULE_ID, DOCTOR_ID, SCHEDULE_START_DATE, SCHEDULE_END_DATE) VALUES (123, 103, TO_TIMESTAMP('2023-07-01 09:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2023-07-05 18:00:00', 'YYYY-MM-DD HH24:MI:SS'));
UPDATE SCHEDULE_DETAIL SET SCHEDULE_ID = 123 WHERE SCHEDULE_DETAIL_ID in (116, 118);
INSERT INTO SCHEDULE(SCHEDULE_ID, DOCTOR_ID, SCHEDULE_START_DATE, SCHEDULE_END_DATE) VALUES (124, 104, TO_TIMESTAMP('2023-07-01 09:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2023-07-05 18:00:00', 'YYYY-MM-DD HH24:MI:SS'));
UPDATE SCHEDULE_DETAIL SET SCHEDULE_ID = 124 WHERE SCHEDULE_DETAIL_ID = 117;
INSERT INTO SCHEDULE(SCHEDULE_ID, DOCTOR_ID, SCHEDULE_START_DATE, SCHEDULE_END_DATE) VALUES (125, 105, TO_TIMESTAMP('2023-07-01 09:00:00', 'YYYY-MM-DD HH24:MI:SS'), TO_TIMESTAMP('2023-07-05 18:00:00', 'YYYY-MM-DD HH24:MI:SS'));
UPDATE SCHEDULE_DETAIL SET SCHEDULE_ID = 125 WHERE SCHEDULE_DETAIL_ID in (119, 121);