/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.us.dp1.l4_01_24_25.upstream.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.model.BaseRestController;
import es.us.dp1.l4_01_24_25.upstream.statistic.Achievement;
import es.us.dp1.l4_01_24_25.upstream.user.DTO.UserListAdmin;
import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievementService;

@RestController
@RequestMapping("/api/v1/users")
class UserRestController extends BaseRestController<User,Integer>{

	UserService userService;
	UserAchievementService userAchievementService;

	@Autowired
	public UserRestController(UserService userService, UserAchievementService userAchievementService) {
		super(userService);
		this.userService = userService;
		this.userAchievementService = userAchievementService;
	}

	@GetMapping("authorities")
	public ResponseEntity<Authority[]> findAllAuths() {
		return new ResponseEntity<>(Authority.values(), HttpStatus.OK);
	}

	@GetMapping("/{userId}/achievements")
	public ResponseEntity<List<Achievement>> findUserAchievements(@PathVariable("userId") int userId) {
		return new ResponseEntity<>(this.userAchievementService.findUserAchievements(userId), HttpStatus.OK);
	}

	@PatchMapping(value = "/listing/{userId}")
	public ResponseEntity<UserListAdmin> updateUserAuthority(@PathVariable("userId") Integer id) {
		return new ResponseEntity<>(this.userService.updateUserAuthority(id), HttpStatus.OK);
	}

	@GetMapping("/listing")
	public ResponseEntity<List<UserListAdmin>> findAllListing() {
		return new ResponseEntity<>(this.userService.findAllListing(), HttpStatus.OK);
	}

}