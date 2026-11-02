package com.nicky.ourspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.application.usecases.room.SaveRoomUseCase;
import com.nicky.ourspace.application.usecases.room.EnsureUserRoomsUseCase;
import com.nicky.ourspace.application.usecases.roommember.AddRoomMemberUseCase;
import com.nicky.ourspace.application.usecases.roommember.RemoveRoomMemberUseCase;
import com.nicky.ourspace.application.usecases.user.SaveUserUseCase;
import com.nicky.ourspace.domain.exception.OwnerMembershipCannotBeRemovedException;
import com.nicky.ourspace.domain.exception.RoomMemberAlreadyExistsException;
import com.nicky.ourspace.domain.model.RoomMemberRole;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RoomMemberId;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.UserDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaRoomMemberRepository;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaUserRepository;

@SpringBootTest(properties = "security.jwt.secret=test-only-signing-key-with-at-least-32-bytes")
@Transactional
class OurspaceApplicationTests {

	private final SaveRoomUseCase saveRoomUseCase;
	private final EnsureUserRoomsUseCase ensureUserRoomsUseCase;
	private final SaveUserUseCase saveUserUseCase;
	private final AddRoomMemberUseCase addRoomMemberUseCase;
	private final RemoveRoomMemberUseCase removeRoomMemberUseCase;
	private final JpaUserRepository jpaUserRepository;
	private final JpaRoomMemberRepository jpaRoomMemberRepository;

	@Autowired
	OurspaceApplicationTests(
			SaveRoomUseCase saveRoomUseCase,
			EnsureUserRoomsUseCase ensureUserRoomsUseCase,
			SaveUserUseCase saveUserUseCase,
			AddRoomMemberUseCase addRoomMemberUseCase,
			RemoveRoomMemberUseCase removeRoomMemberUseCase,
			JpaUserRepository jpaUserRepository,
			JpaRoomMemberRepository jpaRoomMemberRepository) {
		this.saveRoomUseCase = saveRoomUseCase;
		this.ensureUserRoomsUseCase = ensureUserRoomsUseCase;
		this.saveUserUseCase = saveUserUseCase;
		this.addRoomMemberUseCase = addRoomMemberUseCase;
		this.removeRoomMemberUseCase = removeRoomMemberUseCase;
		this.jpaUserRepository = jpaUserRepository;
		this.jpaRoomMemberRepository = jpaRoomMemberRepository;
	}

	@Test
	void contextLoads() {
	}

	@Test
	void roomCreationAddsOwnerMembershipAndMembersUseCompositeId() {
		UUID ownerId = UUID.randomUUID();
		UUID memberId = UUID.randomUUID();
		jpaUserRepository.save(user(ownerId));
		jpaUserRepository.save(user(memberId));

		var savedRoom = saveRoomUseCase.execute(
				"room-" + UUID.randomUUID(),
				"description",
				null,
				ownerId);

		var ownerMembershipId = new RoomMemberId(savedRoom.id(), ownerId);
		assertEquals(
				RoomMemberRole.OWNER,
				jpaRoomMemberRepository.findById(ownerMembershipId).orElseThrow().getRole());

		addRoomMemberUseCase.execute(savedRoom.id(), memberId, memberId);
		var memberMembershipId = new RoomMemberId(savedRoom.id(), memberId);
		assertEquals(
				RoomMemberRole.MEMBER,
				jpaRoomMemberRepository.findById(memberMembershipId).orElseThrow().getRole());
		assertThrows(
				RoomMemberAlreadyExistsException.class,
				() -> addRoomMemberUseCase.execute(savedRoom.id(), memberId, memberId));

		jpaRoomMemberRepository.deleteById(ownerMembershipId);
		jpaRoomMemberRepository.synchronizeOwnerMemberships();
		assertEquals(
				RoomMemberRole.OWNER,
				jpaRoomMemberRepository.findById(ownerMembershipId).orElseThrow().getRole());

		assertThrows(
				OwnerMembershipCannotBeRemovedException.class,
				() -> removeRoomMemberUseCase.execute(savedRoom.id(), ownerId, ownerId));
		removeRoomMemberUseCase.execute(savedRoom.id(), memberId, memberId);
		assertTrue(jpaRoomMemberRepository.findById(ownerMembershipId).isPresent());
		assertFalse(jpaRoomMemberRepository.findById(memberMembershipId).isPresent());
	}

	@Test
	void newUsersAreCreatedWithAnOwnedRoom() {
		String username = "new-" + UUID.randomUUID();
		saveUserUseCase.execute(username, "password", "555-0100", username + "@example.com");

		var savedUser = jpaUserRepository.findByUsername(username).orElseThrow();
		var memberships = jpaRoomMemberRepository.findAllByUser_Id(savedUser.getId());

		assertEquals(1, memberships.size());
		assertEquals(RoomMemberRole.OWNER, memberships.get(0).getRole());
		assertEquals(savedUser.getId(), memberships.get(0).getRoom().getOwner().getId());
	}

	@Test
	void existingUsersWithoutRoomsReceiveOneAndBackfillIsIdempotent() {
		UUID userId = UUID.randomUUID();
		jpaUserRepository.save(user(userId));

		ensureUserRoomsUseCase.ensureRoomsForAllUsers();
		ensureUserRoomsUseCase.ensureRoomsForAllUsers();

		var memberships = jpaRoomMemberRepository.findAllByUser_Id(userId);
		assertEquals(1, memberships.size());
		assertEquals(RoomMemberRole.OWNER, memberships.get(0).getRole());
		assertEquals(userId, memberships.get(0).getRoom().getOwner().getId());
	}

	private UserDbo user(UUID id) {
		String unique = id.toString();
		return new UserDbo(id, unique, unique + "@example.com", unique, "password-hash");
	}
}
