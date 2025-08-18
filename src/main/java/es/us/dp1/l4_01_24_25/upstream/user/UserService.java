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

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.AccessDeniedException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.model.BaseService;
import es.us.dp1.l4_01_24_25.upstream.statistic.Achievement;
import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievementRepository;
import jakarta.validation.Valid;

@Service
public class UserService extends BaseService<User,Integer>{

	UserRepository userRepository;
	UserAchievementRepository userAchievementRepository;

	@Autowired
	public UserService(UserRepository userRepository, UserAchievementRepository userAchievementRepository) {
		super(userRepository);
		this.userAchievementRepository = userAchievementRepository;
	}

	@Transactional(readOnly = true)
	public User findUserByName(String username) {
		return this.findOrResourceNotFoundException(this.userRepository.findByName(username), "username", username);
	}

	@Transactional(readOnly = true)
	public User findCurrentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			throw new ResourceNotFoundException("Nobody authenticated!");
		else
			return this.findOrResourceNotFoundException(this.userRepository.findByName(auth.getName()), "auth", auth);
	}

	public Boolean existsUserByName(String username) {
		return this.userRepository.existsByName(username);
	}

	public Iterable<User> findAllByAuthority(String auth) {
		return this.userRepository.findAllByAuthority(auth);
	}

	@Transactional
	public User updateUser(@Valid User user, Integer idToUpdate) {
		User toUpdate = this.findById(idToUpdate);
		BeanUtils.copyProperties(user, toUpdate, "id");
		return this.save(toUpdate);
	}
	
	@Transactional
	public List<Achievement> findUserAchievements(Integer userId) {
		return this.userAchievementRepository.findByUserId(userId).stream().map(userAchivement -> userAchivement.getAchievement()).toList();
	}

	@Transactional
	public void delete(Integer id) {
		if (this.findCurrentUser().getId() != id) {
			this.deleteById(id);
		} else
			throw new AccessDeniedException("You can't delete yourself!");
	}

}