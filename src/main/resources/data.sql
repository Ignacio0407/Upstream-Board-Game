-- One admin user, named admin1 with passwor 4dm1n and authority admin
INSERT INTO authorities(id,authority) VALUES (1,'ADMIN');
INSERT INTO appusers(id,username,password,authority,victories,playedgames,totalpoints) VALUES (1,'admin1','$2a$10$nMmTWAhPTqXqLDJTag3prumFrAJpsYtroxf0ojesFYq0k4PmcbWUS',1,20,30,100);

-- Ten player users, named player1 with passwor 0wn3r
INSERT INTO authorities(id,authority) VALUES (2,'PLAYER');
INSERT INTO appusers(id,username,password,authority,victories,playedgames,totalpoints) VALUES (4,'player1','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,30,40,120);
INSERT INTO appusers(id,username,password,authority,victories,playedgames,totalpoints) VALUES (5,'player2','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,30,40,120);
INSERT INTO appusers(id,username,password,authority,victories,playedgames,totalpoints) VALUES (6,'player3','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,30,40,120);
INSERT INTO appusers(id,username,password,authority,victories,playedgames,totalpoints) VALUES (7,'player4','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,30,40,120);
INSERT INTO appusers(id,username,password,authority,victories,playedgames,totalpoints) VALUES (8,'player5','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,30,40,120);
INSERT INTO appusers(id,username,password,authority,victories,playedgames,totalpoints) VALUES (9,'player6','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,30,40,120);
INSERT INTO appusers(id,username,password,authority,victories,playedgames,totalpoints) VALUES (10,'player7','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,30,80,400);
INSERT INTO appusers(id,username,password,authority,victories,playedgames,totalpoints) VALUES (11,'player8','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,50,100,1000);
INSERT INTO appusers(id,username,password,authority,victories,playedgames,totalpoints) VALUES (12,'player9','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,20,40,500);
INSERT INTO appusers(id,username,password,authority,victories,playedgames,totalpoints) VALUES (13,'player10','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,10,20,20);
INSERT INTO appusers(id,username,password,authority,victories,playedgames,totalpoints) VALUES (14,'KCG5658','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,3,10,40);
INSERT INTO appusers(id,username,password,authority,victories,playedgames,totalpoints) VALUES (15,'FSL4030','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,5,20,60);
INSERT INTO appusers(id,username,password,authority,victories,playedgames,totalpoints) VALUES (16,'NHD9927','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,10,15,200);
INSERT INTO appusers(id,username,password,authority,victories,playedgames,totalpoints) VALUES (17,'NBL3749','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,1,5,10);
INSERT INTO appusers(id,username,password,authority,victories,playedgames,totalpoints) VALUES (18,'SDL0654','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,2,10,20);

INSERT INTO achievement(id, name, description, threshold, badge_image, metric) VALUES 
(1, 'Basic experience', 'You have played more than 10 games', 10.0, 'https://cdn-icons-png.flaticon.com/512/5243/5243423.png', 'GAMES_PLAYED'),
(2, 'Explorer', 'You have played more than 25 games', 25.0, 'https://cdn-icons-png.flaticon.com/512/603/603855.png', 'GAMES_PLAYED'),
(3, 'Expert', 'You have won more than 20 games', 20.0, NULL, 'VICTORIES'),
(4, 'Eager', 'You have consulted the rules', 1, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRGX5UO6OsMsjDLmXv_q7qwndH59iXU_B-o20u_D3rYDC3Wknxkezh3tbKYF9aqpelKV4o&usqp=CAU', 'EXPLORER'),
(5, 'Gambler', 'You have won more than 100 games', 100.0, NULL, 'TOTAL_POINTS');

INSERT INTO appusers_logros(user_id, achievement_id) VALUES (4, 1);
INSERT INTO appusers_logros(user_id, achievement_id) VALUES (4, 2);
INSERT INTO appusers_logros(user_id, achievement_id) VALUES (4, 3);

/*
INSERT INTO player (id, name, color, player_order, alive, points, user_player) VALUES 
(1, 'Juan', 'ROJO', 1, TRUE, 10, 1),
(2, 'Maria', 'ROJO', 2, TRUE, 20, 1),
(3, 'Carlos', 'VERDE', 3, FALSE, 15, 4),
(4, 'Ana', 'AMARILLO', 4, TRUE, 25, 8),
(5, 'Luis', 'VERDE', 5, FALSE, 30, 4),
(6, 'Elena', 'MORADO', 6, TRUE, 5, 5),
(7, 'Pedro', 'BLANCO', 7, TRUE, 12, 6);


INSERT INTO matches (id, name, players_num, password, state, round, phase, initial_player, actual_player, match_creator) VALUES 
(1, 'Partida1', 2, '1234', 'ESPERANDO', 1, 'MOVIENDO', 1, 1, 4),
(2, 'Partida2', 3, 'abcd', 'EN_CURSO', 2, 'CASILLAS', 2, 2, 5),
(3, 'Partida3', 4, '5678', 'FINALIZADA', 3, 'MOVIENDO', 3, 3, 6),
(4, 'Partida4', 5, 'efgh', 'ESPERANDO', 1, 'CASILLAS', 4, 4, 7),
(5, 'Partida5', 2, 'ijkl', 'EN_CURSO', 2, 'MOVIENDO', 5, 5, 8);


UPDATE player SET partida = 1 WHERE id=1;
UPDATE player SET partida = 1 WHERE id=2;
UPDATE player SET partida = 1 WHERE id=3;
*/

INSERT INTO tile_type(id, type) VALUES 
(1, 'AGUA'),
(2, 'PIEDRA'),
(3, 'GARZA'),
(4, 'OSO'),
(5, 'AGUILA'),
(6, 'SALTO'),
(7, 'MAR'),
(8, 'DESOVE');

INSERT INTO tile (id, image, tile_type) VALUES 
(1, 'src/main/resources/images/waterTile.png', 1),
(2, 'src/main/resources/images/rockTile.png', 2),
(3, 'src/main/resources/images/heronTile.png', 3),
(4, 'src/main/resources/images/bearTile.png', 4),
(5, 'src/main/resources/images/eagleTile.png', 5),
(6, 'src/main/resources/images/jumpTile.png', 6),
(7, 'src/main/resources/images/seaTile.png', 7),
(8, 'src/main/resources/images/spawningTile.png', 8);

INSERT INTO salmon (id, color, image) VALUES
(1, 'AMARILLO', 'src/main/resources/images/Amarillo_2.png'),
(2, 'BLANCO', 'src/main/resources/images/Blanco_2.png'),
(3, 'ROJO', 'src/main/resources/images/Rojo_2.png'),
(4, 'MORADO', 'src/main/resources/images/Morado_2.png'),
(5, 'VERDE', 'src/main/resources/images/Verde_2.png');