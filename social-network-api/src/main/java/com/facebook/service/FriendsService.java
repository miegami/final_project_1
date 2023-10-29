package com.facebook.service;

import com.facebook.dto.appuser.AppUserResponse;
import com.facebook.dto.friends.FriendsResponse;
import com.facebook.exception.AlreadyExistsException;
import com.facebook.exception.NotFoundException;
import com.facebook.facade.AppUserFacade;
import com.facebook.facade.FriendsFacade;
import com.facebook.model.AppUser;
import com.facebook.model.friends.Friends;
import com.facebook.model.friends.FriendsStatus;
import com.facebook.repository.AppUserRepository;
import com.facebook.repository.FriendsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendsService {

    private final FriendsRepository friendsRepository;

    private final AppUserRepository appUserRepository;

    private final FriendsFacade facade;

    private final AppUserFacade userFacade;

    private static final String FRIENDS_NOT_FOUND_ERROR_MSG = "Friends pair not found";

    public FriendsResponse sendFriendRequest(Long userId, Long friendId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found!"));
        AppUser friend = appUserRepository.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Friend not found!"));

        Optional<Friends> duplicates = friendsRepository.findFriendsByUserIdAndFriendId(userId, friendId);
        if(duplicates.isPresent()) {
            throw new AlreadyExistsException("Friend request already sent!");
        }

        Optional<Friends> sentRequest = friendsRepository.findFriendsByUserIdAndFriendId(friendId, userId);
        if(sentRequest.isPresent()) {
            throw new AlreadyExistsException("Friend already sent you the request. Check your friend requests!");
        }

        Friends friendRequest = new Friends(user, friend, FriendsStatus.PENDING);
        friendsRepository.save(friendRequest);

        return facade.toFriendsResponse(friendRequest);
    }

    public void changeFriendsStatus(Long userId, Long friendId, Boolean status) {
        friendsRepository.findFriendsByUserIdAndFriendId(userId, friendId).ifPresentOrElse(
                f -> {
                    if (Boolean.TRUE.equals(status)) {
                        f.setStatus(FriendsStatus.APPROVED);
                        friendsRepository.save(f);

                        friendsRepository.save(new Friends(f.getFriend(), f.getUser(),FriendsStatus.APPROVED));
                    }
                    else {
                        f.setStatus(FriendsStatus.REJECTED);
                        friendsRepository.delete(f);
                    }
                },
                () -> {
                    throw new NotFoundException(FRIENDS_NOT_FOUND_ERROR_MSG);
                }
        );
    }

    public void deleteFriend(Long userId, Long friendId) {
        friendsRepository.findFriendsByUserIdAndFriendId(userId, friendId).ifPresentOrElse(
                (f) -> {
                    System.out.printf("asdf 1 ---> %s\n", f);
                    friendsRepository.delete(f);
                },
                () -> {
                    throw new NotFoundException(FRIENDS_NOT_FOUND_ERROR_MSG);
                }
        );
        friendsRepository.findFriendsByUserIdAndFriendId(friendId, userId).ifPresentOrElse(
                (f) -> {
                    System.out.printf("asdf 2 ---> %s\n", f);
                    friendsRepository.delete(f);
                },
                () -> {
                    throw new NotFoundException(FRIENDS_NOT_FOUND_ERROR_MSG);
                }
        );
    }

//    public void deleteFriend1(Long userId, Long friendId) {
//        friendsRepository.deleteFriendByUserIdAndFriendId(userId, friendId);
//    }

    public List<AppUserResponse> getFriendsByUserId(Long id) {
        return friendsRepository.findFriendsByUserId(id)
                .stream()
//                .map(facade::toFriendsResponse)
                .map(userFacade::convertToAppUserResponse)
                .toList();
    }

    public List<FriendsResponse> getFriendsRequest(Long userId) {
        return friendsRepository.findFriendsRequestsByUserId(userId)
                .stream()
                .map(facade::toFriendsResponse)
                .toList();
    }

}
