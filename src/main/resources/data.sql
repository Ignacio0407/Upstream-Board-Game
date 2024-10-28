-- One admin user, named admin1 with passwor 4dm1n and authority admin
INSERT INTO authorities(id,authority) VALUES (1,'ADMIN');
INSERT INTO appusers(id,username,password,authority,victorias,partidasjugadas,puntostotales) VALUES (1,'admin1','$2a$10$nMmTWAhPTqXqLDJTag3prumFrAJpsYtroxf0ojesFYq0k4PmcbWUS',1,20,30,100);

-- Ten player users, named player1 with passwor 0wn3r
INSERT INTO authorities(id,authority) VALUES (2,'PLAYER');
INSERT INTO appusers(id,username,password,authority,victorias,partidasjugadas,puntostotales) VALUES (4,'player1','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,30,40,120);
INSERT INTO appusers(id,username,password,authority,victorias,partidasjugadas,puntostotales) VALUES (5,'player2','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,30,40,120);
INSERT INTO appusers(id,username,password,authority,victorias,partidasjugadas,puntostotales) VALUES (6,'player3','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,30,40,120);
INSERT INTO appusers(id,username,password,authority,victorias,partidasjugadas,puntostotales) VALUES (7,'player4','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,30,40,120);
INSERT INTO appusers(id,username,password,authority,victorias,partidasjugadas,puntostotales) VALUES (8,'player5','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,30,40,120);
INSERT INTO appusers(id,username,password,authority,victorias,partidasjugadas,puntostotales) VALUES (9,'player6','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,30,40,120);
INSERT INTO appusers(id,username,password,authority,victorias,partidasjugadas,puntostotales) VALUES (10,'player7','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,30,80,400);
INSERT INTO appusers(id,username,password,authority,victorias,partidasjugadas,puntostotales) VALUES (11,'player8','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,50,100,1000);
INSERT INTO appusers(id,username,password,authority,victorias,partidasjugadas,puntostotales) VALUES (12,'player9','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,20,40,500);
INSERT INTO appusers(id,username,password,authority,victorias,partidasjugadas,puntostotales) VALUES (13,'player10','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,10,20,20);
INSERT INTO appusers(id,username,password,authority,victorias,partidasjugadas,puntostotales) VALUES (14,'KCG5658','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,3,10,40);
INSERT INTO appusers(id,username,password,authority,victorias,partidasjugadas,puntostotales) VALUES (15,'FSL4030','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,5,20,60);
INSERT INTO appusers(id,username,password,authority,victorias,partidasjugadas,puntostotales) VALUES (16,'NHD9927','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,10,15,200);
INSERT INTO appusers(id,username,password,authority,victorias,partidasjugadas,puntostotales) VALUES (17,'NBL3749','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,1,5,10);
INSERT INTO appusers(id,username,password,authority,victorias,partidasjugadas,puntostotales) VALUES (18,'SDL0654','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,2,10,20);
INSERT INTO appusers(id,username,password,authority,victorias,partidasjugadas,puntostotales) VALUES (19,'antlopcub','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,0,2,20);

INSERT INTO achievement(id,name,description,threshold,badge_image,metric) VALUES (1,'Experiencia basica','Si juegas 10 partidas o mas',10.0,'https://cdn-icons-png.flaticon.com/512/5243/5243423.png','GAMES_PLAYED');
INSERT INTO achievement(id,name,description,threshold,badge_image,metric) VALUES (2,'Explorador','Si juegas 25 partidas o mas',25.0,'https://cdn-icons-png.flaticon.com/512/603/603855.png','GAMES_PLAYED');
INSERT INTO achievement(id,name,description,threshold,metric) VALUES (3,'Experto','Si ganas 20 partidas o mas',20.0 ,'VICTORIES');
INSERT INTO achievement(id,name,description,threshold,badge_image,metric) VALUES (4,'Curioso','Consulta las reglas',11.0, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRGX5UO6OsMsjDLmXv_q7qwndH59iXU_B-o20u_D3rYDC3Wknxkezh3tbKYF9aqpelKV4o&usqp=CAU', 'EXPLORER');

--ALTER TABLE matches ALTER COLUMN estado SET DATA TYPE VARCHAR(255);
--ALTER TABLE matches ALTER COLUMN fase SET DATA TYPE VARCHAR(255);

INSERT INTO jugador(id, name, color, orden, vivo, puntos) VALUES (2, 'Jugador2', 'ROJO', 3, false, 7);
INSERT INTO jugador(id, name, color, orden, vivo, puntos) VALUES (4, 'Jugador4', 'AMARILLO', 1, true, 14);

INSERT INTO matches(id,name,contrasena,estado,num_jugadores,ronda,fase,jugador_inicial,jugador_actual) VALUES (1,'Prueba','Pikachu','EN_CURSO',4,2,'MOVIENDO', 2, 4);