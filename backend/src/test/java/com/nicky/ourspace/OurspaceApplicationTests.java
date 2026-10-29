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
import com.nicky.ourspace.application.usecases.roommember.AddRoomMemberUseCase;
import com.nicky.ourspace.application.usecases.roommember.RemoveRoomMemberUseCase;
import com.nicky.ourspace.domain.exception.OwnerMembershipCannotBeRemovedException;
import com.nicky.ourspace.domain.exception.RoomMemberAlreadyExistsException;
import com.nicky.ourspace.domain.model.RoomMemberRole;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RoomMemberId;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.UserDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaRoomMemberRepository;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaUserRepository;

@SpringBootTest
@Transactional
class OurspaceApplicationTests {

	private final SaveRoomUseCase saveRoomUseCase;
	private final AddRoomMemberUseCase addRoomMemberUseCase;
	private final RemoveRoomMemberUseCase removeRoomMemberUseCase;
	private final JpaUserRepository jpaUserRepository;
	private final JpaRoomMemberRepository jpaRoomMemberRepository;

	@Autowired
	OurspaceApplicationTests(
			SaveRoomUseCase saveRoomUseCase,
			AddRoomMemberUseCase addRoomMemberUseCase,
			RemoveRoomMemberUseCase removeRoomMemberUseCase,
			JpaUserRepository jpaUserRepository,
			JpaRoomMemberRepository jpaRoomMemberRepository) {
		this.saveRoomUseCase = saveRoomUseCase;
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

	private UserDbo user(UUID id) {
		String unique = id.toString();
		return new UserDbo(id, unique, unique + "@example.com", unique, "password-hash");
	}
}
