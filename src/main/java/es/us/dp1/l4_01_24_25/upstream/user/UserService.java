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
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.AccessDeniedException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.model.BaseService;
import es.us.dp1.l4_01_24_25.upstream.statistic.Achievement;
import es.us.dp1.l4_01_24_25.upstream.user.DTO.UserListAdmin;
import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievementService;

@Service
public class UserService extends BaseService<User,Integer> {

	UserRepository userRepository;
	UserAchievementService userAchievementService;

	@Autowired
	public UserService(UserRepository userRepository, @Lazy UserAchievementService userAchievementService) {
		super(userRepository);
		this.userRepository = userRepository;
		this.userAchievementService = userAchievementService;
	}

	@Override
    @Transactional
    protected User updateEntityFields(User existingUser, User newUser) {
        existingUser.setName(newUser.getName());
        existingUser.setAuthority(newUser.getAuthority());
        return existingUser;
    }

	public List<Authority> findAllAuthorities() {
		return List.of(Authority.values());
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

	public List<User> findAllByAuthority(Authority auth) {
		return this.userRepository.findAllByAuthority(auth);
	}

	@Transactional
	public UserListAdmin updateUserAuthority(Integer idToUpdate) {
		User toUpdate = this.findById(idToUpdate);
		if (toUpdate.getAuthority().equals(Authority.PLAYER)) toUpdate.setAuthority(Authority.ADMIN);
		else toUpdate.setAuthority(Authority.PLAYER);
		this.save(toUpdate);
		return new UserListAdmin(idToUpdate, toUpdate.getName(), toUpdate.getAuthority());
	}
	
	@Transactional
	public List<Achievement> findUserAchievements(Integer userId) {
		this.userAchievementService.checkAndUnlockAchievements(userId);
		return this.userAchievementService.findByUserId(userId).stream().map(ua -> ua.getAchievement()).toList();
	}

	@Transactional
	public void delete(Integer id) {
		if (this.findCurrentUser().getId() != id) {
			this.deleteById(id);
		} else
			throw new AccessDeniedException("You can't delete yourself!");
	}

	@Transactional
	public List<UserListAdmin> findAllListing() {
		if (!this.findCurrentUser().hasAuthority(Authority.ADMIN)) {
			throw new AccessDeniedException("Only admins can list all users!");
		}
		return this.userRepository.findAllListAdmin();
	}

}