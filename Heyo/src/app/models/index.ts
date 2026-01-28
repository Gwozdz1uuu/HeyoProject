// User related models
export interface User {
  id: number;
  username: string;
  email: string;
  avatarUrl?: string;
  online: boolean;
  lastSeen?: string;
}

export interface AuthRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  userId: number;
  username: string;
  email: string;
  avatarUrl?: string;
  profileCompleted?: boolean;
}

export interface Interest {
  id: number;
  name: string;
}

export interface ProfileCreateRequest {
  firstName: string;
  lastName: string;
  nickname?: string;
  avatarUrl?: string;
  interestIds: number[];
}

export interface ProfileDTO {
  id: number;
  userId: number;
  username: string;
  email: string;
  avatarUrl?: string;
  firstName?: string;
  lastName?: string;
  nickname?: string;
  bio?: string;
  dateOfBirth?: string;
  location?: string;
  website?: string;
  phoneNumber?: string;
  friendsCount: number;
  postsCount: number;
  newToken?: string; // New JWT token if username was changed
}

// Post related models
export interface Post {
  id: number;
  authorId: number;
  authorUsername: string;
  authorAvatarUrl?: string;
  content?: string;
  imageUrl?: string;
  likesCount: number;
  commentsCount: number;
  likedByCurrentUser: boolean;
  createdAt: string;
}

export interface PostCreateRequest {
  content?: string;
  imageUrl?: string;
}

export interface Comment {
  id: number;
  postId: number;
  authorId: number;
  authorUsername: string;
  authorAvatarUrl?: string;
  content: string;
  createdAt: string;
}

// Event related models
export interface Event {
  id: number;
  title: string;
  description?: string;
  imageUrl?: string;
  eventDate: string;
  location?: string;
  hashtags?: string;
  creatorId: number;
  creatorUsername: string;
  interestedCount: number;
  participantsCount: number;
  isInterested: boolean;
  isParticipating: boolean;
  createdAt: string;
}

export interface EventCreateRequest {
  title: string;
  description?: string;
  imageUrl?: string;
  eventDate: string;
  location?: string;
  hashtags?: string;
}

// Notification model
export interface Notification {
  id: number;
  actorUsername?: string;
  actorAvatarUrl?: string;
  type: string;
  message?: string;
  referenceId?: number;
  read: boolean;
  createdAt: string;
}

// Chat related models
export interface Conversation {
  id: number;
  partnerId: number;
  partnerUsername: string;
  partnerAvatarUrl?: string;
  partnerOnline: boolean;
  lastMessage?: string;
  lastMessageAt?: string;
  unreadCount: number;
}

export interface ChatMessage {
  id: number;
  senderId: number;
  senderUsername: string;
  senderAvatarUrl?: string;
  receiverId: number;
  receiverUsername: string;
  content: string;
  read: boolean;
  createdAt: string;
}

// Paginated response
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// API Error response
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path?: string;
  validationErrors?: Record<string, string>;
}
