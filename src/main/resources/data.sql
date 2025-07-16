-- One admin user, named admin1 with passwor 4dm1n and authority admin
INSERT INTO authorities(id,authority) VALUES (1,'ADMIN');
INSERT INTO appusers(id,name,password,authority,victories,playedgames,totalpoints) VALUES (1,'admin1','$2a$10$nMmTWAhPTqXqLDJTag3prumFrAJpsYtroxf0ojesFYq0k4PmcbWUS',1,20,30,100);

-- Ten player users, named player1 with passwor 0wn3r
INSERT INTO authorities(id,authority) VALUES (2,'PLAYER');
INSERT INTO appusers(id,name,password,authority,victories,playedgames,totalpoints) VALUES (2,'player1','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,0,9,99);
INSERT INTO appusers(id,name,password,authority,victories,playedgames,totalpoints) VALUES (3,'player2','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,0,9,99);
INSERT INTO appusers(id,name,password,authority,victories,playedgames,totalpoints) VALUES (4,'player3','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,0,9,99);
INSERT INTO appusers(id,name,password,authority,victories,playedgames,totalpoints) VALUES (5,'player4','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,30,40,120);
INSERT INTO appusers(id,name,password,authority,victories,playedgames,totalpoints) VALUES (6,'player5','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,30,40,120);
INSERT INTO appusers(id,name,password,authority,victories,playedgames,totalpoints) VALUES (7,'KCG5658','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,3,10,40);
INSERT INTO appusers(id,name,password,authority,victories,playedgames,totalpoints) VALUES (8,'FSL4030','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,5,20,60);
INSERT INTO appusers(id,name,password,authority,victories,playedgames,totalpoints) VALUES (9,'NHD9927','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,10,15,200);
INSERT INTO appusers(id,name,password,authority,victories,playedgames,totalpoints) VALUES (10,'NBL3749','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,1,5,10);
INSERT INTO appusers(id,name,password,authority,victories,playedgames,totalpoints) VALUES (11,'SDL0654','$2a$10$DaS6KIEfF5CRTFrxIoGc7emY3BpZZ0.fVjwA3NiJ.BjpGNmocaS3e',2,2,10,20);

INSERT INTO achievement(id, name, description, threshold, badge_image, metric) VALUES 
(1, 'Basic experience', 'You have played more than 1 game', 1, '/images/achievements/MoreThan1Game.png', 'GAMES_PLAYED'),
(2, 'Moderate experience', 'You have played more than 10 games', 10.0, '/images/achievements/Explorer.png', 'GAMES_PLAYED'),
(3, 'Expert', 'You have won more than 20 games', 20.0, NULL, 'VICTORIES'),
(4, 'Eager', 'You have consulted the rules', 1, '/images/achievements/Eager.png', 'EXPLORER'),
(5, 'Gambler', 'You have more than 20 points', 100.0, NULL, 'TOTAL_POINTS');

INSERT INTO appusers_achievements(user_id, achievement_id) VALUES 
(4, 1), (4, 2), (4, 3);

INSERT INTO tile (id, image, type) VALUES 
(1, '/images/tiles/water.png', 'WATER'),
(2, '/images/tiles/rock.png', 'ROCK'),
(3, '/images/tiles/heron.png', 'HERON'),
(4, '/images/tiles/bear.png', 'BEAR'),
(5, '/images/tiles/eagle.png', 'EAGLE'),
(6, '/images/tiles/jump.png', 'JUMP'),
(7, '/images/tiles/sea.png', 'SEA'),
(8, '/images/tiles/spawning.png', 'SPAWNING');

INSERT INTO salmon (id, color, image) VALUES
(1, 'YELLOW', '/images/salmons/Yellow_2.png'),
(2, 'WHITE', '/images/salmons/White_2.png'),
(3, 'RED', '/images/salmons/Red_2.png'),
(4, 'PURPLE', '/images/salmons/Purple_2.png'),
(5, 'GREEN', '/images/salmons/Green_2.png'),
(6, 'YELLOW', '/images/salmons/Yellow_1.png'),
(7, 'WHITE', '/images/salmons/White_1.png'),
(8, 'RED', '/images/salmons/Red_1.png'),
(9, 'PURPLE', '/images/salmons/Purple_1.png'),
(10, 'GREEN', '/images/salmons/Green_1.png');