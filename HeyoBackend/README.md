# Heyo Backend - API Serwer

## Opis Projektu

Heyo Backend to serwer API zbudowany w Spring Boot, który dostarcza funkcjonalności backendowe dla aplikacji społecznościowej Heyo. Serwer obsługuje autoryzację użytkowników, zarządzanie postami, komunikację w czasie rzeczywistym przez WebSocket, zarządzanie znajomymi, wydarzeniami oraz powiadomieniami. API jest zbudowane w architekturze RESTful z komunikacją w czasie rzeczywistym przez protokół STOMP przez WebSocket.

## Technologie i Narzędzia

### Spring Boot

Aplikacja została zbudowana przy użyciu Spring Boot 3.2.1, który jest frameworkiem Java opartym na Spring Framework. Spring Boot zapewnia automatyczną konfigurację, wbudowany serwer aplikacyjny oraz łatwe zarządzanie zależnościami.

### Spring Security

Spring Security jest używany do zabezpieczenia aplikacji. Implementuje autoryzację opartą na JWT (JSON Web Tokens), która jest stateless, co oznacza, że serwer nie przechowuje informacji o sesji. Wszystkie żądania HTTP wymagają tokenu JWT w nagłówku Authorization, z wyjątkiem publicznych endpointów takich jak logowanie i rejestracja.

### Spring Data JPA

Spring Data JPA jest używany do komunikacji z bazą danych. JPA (Java Persistence API) pozwala na mapowanie obiektów Java na tabele w bazie danych oraz wykonywanie operacji CRUD. Repositories są interfejsami, które automatycznie generują zapytania SQL na podstawie nazw metod.

### MySQL

Baza danych MySQL jest używana do przechowywania danych aplikacji. Konfiguracja połączenia jest zdefiniowana w pliku application.properties. Baza danych zawiera tabele dla użytkowników, profili, postów, komentarzy, polubień, znajomych, wydarzeń, powiadomień oraz wiadomości czatu.

### Flyway

Flyway jest używany do zarządzania migracjami bazy danych. Migracje są przechowywane w folderze resources/db/migration i są wykonywane automatycznie przy starcie aplikacji. Każda migracja ma numer wersji oraz opis, co pozwala na śledzenie zmian w schemacie bazy danych.

### WebSocket i STOMP

Spring WebSocket jest używany do komunikacji w czasie rzeczywistym. Protokół STOMP (Simple Text Oriented Messaging Protocol) jest używany jako protokół warstwy aplikacji nad WebSocket. STOMP pozwala na subskrybowanie tematów oraz wysyłanie wiadomości do konkretnych destynacji.

### JWT (JSON Web Tokens)

JWT jest używany do autoryzacji użytkowników. Token JWT zawiera informacje o użytkowniku i jest podpisywany sekretnym kluczem. Token jest ważny przez określony czas (domyślnie 24 godziny) i jest wysyłany przez klienta w nagłówku Authorization przy każdym żądaniu.

### Lombok

Lombok jest używany do redukcji boilerplate code w klasach Java. Adnotacje takie jak Data, RequiredArgsConstructor, Builder automatycznie generują gettery, settery, konstruktory oraz inne metody, co znacznie zmniejsza ilość kodu.

### BCrypt

BCrypt jest używany do hashowania haseł użytkowników. Hasła są hashowane przed zapisaniem w bazie danych, co zapewnia bezpieczeństwo nawet w przypadku wycieku danych. BCrypt automatycznie dodaje salt do każdego hasła, co czyni je unikalnymi nawet dla identycznych haseł.

## Struktura Projektu

### Folder src/main/java/org/gwozdz1uu/heyobackend

Główny pakiet aplikacji zawiera wszystkie klasy Java aplikacji.

### Kontrolery (controller)

Kontrolery są odpowiedzialne za obsługę żądań HTTP i zwracanie odpowiedzi. Wszystkie kontrolery są oznaczone adnotacją RestController, co oznacza, że zwracają dane w formacie JSON.

#### AuthController

Kontroler autoryzacji obsługuje logowanie i rejestrację użytkowników. Endpoint /api/auth/register przyjmuje email i hasło, tworzy nowego użytkownika oraz zwraca token JWT. Endpoint /api/auth/login przyjmuje username lub email oraz hasło, weryfikuje dane i zwraca token JWT. Kontroler zawiera również endpointy debugowe do testowania haseł, które powinny być usunięte w produkcji.

#### UserController

Kontroler użytkowników obsługuje operacje związane z użytkownikami. Zawiera endpointy do pobierania informacji o aktualnym użytkowniku, wyszukiwania użytkowników, zarządzania znajomymi oraz wysyłania zaproszeń do znajomych. Endpoint /api/users/friends zwraca listę znajomych aktualnego użytkownika. Endpoint /api/users/search umożliwia wyszukiwanie użytkowników po username.

#### ProfileController

Kontroler profilu obsługuje operacje związane z profilami użytkowników. Endpoint /api/profiles/me zwraca profil aktualnego użytkownika. Endpoint /api/profiles/{userId} zwraca profil konkretnego użytkownika. Endpoint /api/profiles/complete służy do uzupełniania profilu po rejestracji. Endpoint /api/profiles/me PUT służy do aktualizacji profilu.

#### PostController

Kontroler postów obsługuje operacje związane z postami. Endpoint /api/posts zwraca feed postów z paginacją. Endpoint /api/posts/user/{userId} zwraca posty konkretnego użytkownika. Endpoint POST /api/posts służy do tworzenia nowych postów. Endpoint POST /api/posts/{id}/like służy do polubienia lub odpolubienia posta. Endpoint POST /api/posts/{id}/comments służy do dodawania komentarzy.

#### EventController

Kontroler wydarzeń obsługuje operacje związane z wydarzeniami. Zawiera endpointy do pobierania listy wydarzeń, tworzenia nowych wydarzeń oraz wyrażania zainteresowania lub uczestnictwa.

#### ChatController

Kontroler czatu obsługuje operacje związane z konwersacjami i wiadomościami. Endpoint /api/chat/conversations zwraca listę konwersacji użytkownika. Endpoint /api/chat/conversations/{conversationId}/messages zwraca wiadomości dla konkretnej konwersacji. Endpoint POST /api/chat/conversations służy do tworzenia nowych konwersacji.

#### NotificationController

Kontroler powiadomień obsługuje operacje związane z powiadomieniami. Endpoint /api/notifications zwraca listę powiadomień użytkownika. Endpoint PUT /api/notifications/{id}/read służy do oznaczania powiadomień jako przeczytane.

#### UploadController

Kontroler uploadu obsługuje przesyłanie plików na serwer. Endpoint POST /api/uploads/avatar służy do przesyłania avatara użytkownika. Pliki są zapisywane w folderze uploads na serwerze, a ścieżka do pliku jest zwracana w odpowiedzi.

#### InterestController

Kontroler zainteresowań obsługuje operacje związane z zainteresowaniami. Endpoint /api/interests zwraca listę dostępnych zainteresowań, które użytkownicy mogą wybrać podczas rejestracji profilu.

### Serwisy (service)

Serwisy zawierają logikę biznesową aplikacji. Są oznaczone adnotacją Service i są wstrzykiwane do kontrolerów przez dependency injection.

#### AuthService

Serwis autoryzacji obsługuje logowanie i rejestrację użytkowników. Metoda register tworzy nowego użytkownika, haszuje hasło przy użyciu BCrypt oraz generuje token JWT. Metoda login weryfikuje dane logowania, porównuje hasło z hashem w bazie danych oraz generuje token JWT. Serwis współpracuje z JwtService do generowania tokenów oraz z UserService do zarządzania użytkownikami.

#### UserService

Serwis użytkowników implementuje interfejs UserDetailsService wymagany przez Spring Security. Metoda loadUserByUsername jest używana przez Spring Security do autoryzacji użytkowników. Serwis obsługuje również operacje związane z znajomymi, takie jak wysyłanie zaproszeń, akceptowanie i odrzucanie zaproszeń oraz usuwanie znajomych. Metoda sendFriendRequest tworzy powiadomienie typu FRIEND_REQUEST dla odbiorcy. Metoda acceptFriendRequest dodaje użytkowników do listy znajomych oraz usuwa powiadomienie. Serwis zarządza również statusem online użytkowników, który jest aktualizowany przez WebSocket.

#### ProfileService

Serwis profilu obsługuje operacje związane z profilami użytkowników. Metoda getProfile zwraca profil użytkownika wraz z zainteresowaniami. Metoda completeProfile tworzy profil po rejestracji, zapisuje zainteresowania oraz może wygenerować nowy token JWT, jeśli username został zmieniony na nickname. Metoda updateProfile aktualizuje dane profilowe użytkownika.

#### PostService

Serwis postów obsługuje operacje związane z postami. Metoda getFeed zwraca feed postów z paginacją, który zawiera posty od znajomych użytkownika. Metoda getUserPosts zwraca posty konkretnego użytkownika. Metoda createPost tworzy nowy post i zapisuje go w bazie danych. Metoda likePost dodaje lub usuwa polubienie posta. Metoda addComment dodaje komentarz do posta.

#### ChatService

Serwis czatu obsługuje operacje związane z konwersacjami i wiadomościami. Metoda getConversations zwraca listę konwersacji użytkownika wraz z ostatnią wiadomością oraz liczbą nieprzeczytanych wiadomości. Metoda getMessages zwraca wiadomości dla konkretnej konwersacji. Metoda sendMessage zapisuje wiadomość w bazie danych oraz zwraca DTO wiadomości, które jest następnie wysyłane przez WebSocket do odbiorcy.

#### EventService

Serwis wydarzeń obsługuje operacje związane z wydarzeniami. Zawiera metody do pobierania listy wydarzeń, tworzenia nowych wydarzeń oraz wyrażania zainteresowania lub uczestnictwa.

#### NotificationService

Serwis powiadomień obsługuje operacje związane z powiadomieniami. Metoda getNotifications zwraca listę powiadomień użytkownika. Metoda markAsRead oznacza powiadomienie jako przeczytane. Metoda createNotification tworzy nowe powiadomienie, które jest używane przez inne serwisy do informowania użytkowników o różnych zdarzeniach.

### Repositories (repository)

Repositories są interfejsami, które rozszerzają JpaRepository i zapewniają dostęp do danych w bazie danych. Spring Data JPA automatycznie generuje implementacje tych interfejsów oraz zapytania SQL na podstawie nazw metod.

#### UserRepository

Repository użytkowników zawiera metody do wyszukiwania użytkowników po username, email oraz wyszukiwania użytkowników zawierających określony tekst w username. Metoda findByUsernameOrEmail pozwala na logowanie zarówno username jak i email.

#### PostRepository

Repository postów zawiera metody do pobierania postów z paginacją oraz zapytania do feedu, które zwracają posty od znajomych użytkownika.

#### ChatMessageRepository

Repository wiadomości czatu zawiera metody do pobierania wiadomości dla konkretnej konwersacji oraz zapytania do znajdowania nieprzeczytanych wiadomości.

#### NotificationRepository

Repository powiadomień zawiera metody do pobierania powiadomień użytkownika oraz wyszukiwania powiadomień po typie i aktorze.

#### ProfileRepository

Repository profilów zawiera metody do pobierania profilów użytkowników wraz z zainteresowaniami.

#### EventRepository

Repository wydarzeń zawiera metody do pobierania wydarzeń z paginacją oraz zapytania do znajdowania wydarzeń użytkownika.

#### CommentRepository

Repository komentarzy zawiera metody do pobierania komentarzy dla konkretnego posta.

#### InterestRepository

Repository zainteresowań zawiera metody do pobierania dostępnych zainteresowań.

### Modele (model)

Modele reprezentują encje w bazie danych. Są oznaczone adnotacją Entity i zawierają mapowanie na tabele w bazie danych przy użyciu adnotacji JPA.

#### User

Model użytkownika reprezentuje użytkownika w systemie. Zawiera pola takie jak id, username, email, password, avatarUrl, online status oraz lastSeen. Model implementuje interfejs UserDetails wymagany przez Spring Security. Zawiera relacje OneToOne z Profile, OneToMany z Post oraz ManyToMany z innymi użytkownikami (znajomi). Hasła są hashowane przy użyciu BCrypt przed zapisaniem w bazie danych.

#### Profile

Model profilu reprezentuje rozszerzone informacje o użytkowniku. Zawiera pola takie jak firstName, lastName, bio, dateOfBirth, location, website oraz phoneNumber. Profile jest powiązany z User relacją OneToOne.

#### Post

Model posta reprezentuje post użytkownika. Zawiera pola takie jak content, imageUrl oraz timestamps. Post jest powiązany z User relacją ManyToOne oraz z Comment relacją OneToMany. Post zawiera również relację ManyToMany z User dla polubień.

#### Comment

Model komentarza reprezentuje komentarz do posta. Zawiera pole content oraz jest powiązany z Post i User relacjami ManyToOne.

#### ChatMessage

Model wiadomości czatu reprezentuje wiadomość w konwersacji. Zawiera pola takie jak content, read status oraz timestamps. Wiadomość jest powiązana z User relacjami ManyToOne dla nadawcy i odbiorcy.

#### Conversation

Model konwersacji reprezentuje konwersację między dwoma użytkownikami. Jest tworzony automatycznie przy pierwszej wiadomości między użytkownikami.

#### Event

Model wydarzenia reprezentuje wydarzenie w aplikacji. Zawiera pola takie jak title, description, imageUrl, eventDate, location oraz hashtags. Wydarzenie jest powiązane z User relacją ManyToOne dla twórcy oraz zawiera relacje ManyToMany z User dla zainteresowanych i uczestników.

#### Notification

Model powiadomienia reprezentuje powiadomienie dla użytkownika. Zawiera pola takie jak type, message, referenceId oraz read status. Powiadomienie jest powiązane z User relacją ManyToOne dla użytkownika oraz aktora. Typ powiadomienia jest reprezentowany przez enum NotificationType, który może być FRIEND_REQUEST, FRIEND_ACCEPTED, POST_LIKE, POST_COMMENT, EVENT_INVITATION lub CHAT_MESSAGE.

#### Interest

Model zainteresowania reprezentuje zainteresowanie, które użytkownicy mogą wybrać podczas rejestracji profilu. Zawiera pola name oraz relację ManyToMany z Profile.

### DTO (Data Transfer Objects)

DTO są obiektami używanymi do transferu danych między warstwami aplikacji. DTO oddzielają wewnętrzną reprezentację danych od zewnętrznego API, co zapewnia bezpieczeństwo oraz elastyczność.

#### AuthRequest i AuthResponse

AuthRequest jest używany do logowania i zawiera username oraz password. AuthResponse jest zwracany po udanym logowaniu lub rejestracji i zawiera token JWT oraz informacje o użytkowniku.

#### RegisterRequest

RegisterRequest jest używany do rejestracji i zawiera email oraz password.

#### UserDTO

UserDTO reprezentuje użytkownika w API i zawiera podstawowe informacje takie jak id, username, email, avatarUrl oraz online status.

#### ProfileDTO

ProfileDTO reprezentuje profil użytkownika i zawiera wszystkie informacje profilowe wraz z zainteresowaniami oraz statystykami takimi jak liczba znajomych i postów.

#### PostDTO

PostDTO reprezentuje post w API i zawiera informacje o autorze, treści posta, obrazie oraz statystykach takich jak liczba polubień i komentarzy.

#### ChatMessageDTO

ChatMessageDTO reprezentuje wiadomość czatu i zawiera informacje o nadawcy, odbiorcy, treści wiadomości oraz statusie przeczytania.

#### ConversationDTO

ConversationDTO reprezentuje konwersację i zawiera informacje o partnerze konwersacji, ostatniej wiadomości oraz liczbie nieprzeczytanych wiadomości.

#### EventDTO

EventDTO reprezentuje wydarzenie i zawiera wszystkie informacje o wydarzeniu wraz z informacjami o twórcy oraz statusie zainteresowania i uczestnictwa.

#### NotificationDTO

NotificationDTO reprezentuje powiadomienie i zawiera informacje o typie powiadomienia, aktorze, wiadomości oraz statusie przeczytania.

### Konfiguracja (config)

Folder config zawiera klasy konfiguracyjne aplikacji.

#### SecurityConfig

SecurityConfig konfiguruje Spring Security. Definiuje SecurityFilterChain, który określa, które endpointy są publiczne, a które wymagają autoryzacji. Publiczne endpointy to /api/auth/**, /ws/**, /api/public/** oraz /api/uploads/**. Wszystkie inne endpointy wymagają autoryzacji. Konfiguracja wyłącza CSRF, ponieważ aplikacja używa JWT, oraz konfiguruje CORS, aby umożliwić żądania z frontendu. Konfiguracja używa stateless session management, co oznacza, że serwer nie przechowuje informacji o sesji.

#### WebSocketConfig

WebSocketConfig konfiguruje WebSocket i STOMP. Konfiguruje message broker, który obsługuje tematy i kolejki STOMP. Endpoint /ws jest zarejestrowany jako STOMP endpoint z obsługą SockJS dla kompatybilności z przeglądarkami. Konfiguracja rejestruje WebSocketAuthInterceptor, który autoryzuje połączenia WebSocket przy użyciu tokenu JWT.

#### WebMvcConfig

WebMvcConfig konfiguruje Spring MVC, głównie CORS oraz inne ustawienia związane z obsługą żądań HTTP.

#### JacksonConfig

JacksonConfig konfiguruje Jackson, który jest używany do serializacji i deserializacji JSON. Konfiguracja może zawierać ustawienia formatowania dat oraz innych typów danych.

### Bezpieczeństwo (security)

Folder security zawiera klasy związane z bezpieczeństwem aplikacji.

#### JwtService

JwtService obsługuje operacje związane z tokenami JWT. Metoda generateToken generuje nowy token JWT dla użytkownika. Metoda extractUsername wyciąga username z tokenu. Metoda isTokenValid weryfikuje, czy token jest ważny i czy odpowiada użytkownikowi. Token jest podpisywany sekretnym kluczem, który jest przechowywany w application.properties jako Base64 encoded string.

#### JwtAuthenticationFilter

JwtAuthenticationFilter jest filtrem Spring Security, który przechwytuje żądania HTTP i weryfikuje token JWT. Filtr wyciąga token z nagłówka Authorization, weryfikuje go przy użyciu JwtService oraz ustawia autoryzację w SecurityContext, jeśli token jest ważny. Filtr jest dodawany przed UsernamePasswordAuthenticationFilter w SecurityFilterChain.

#### WebSocketAuthInterceptor

WebSocketAuthInterceptor jest interceptorem WebSocket, który autoryzuje połączenia WebSocket. Interceptor przechwytuje komendę CONNECT i weryfikuje token JWT z nagłówka Authorization. Jeśli token jest ważny, ustawia Principal w SecurityContext, co pozwala na identyfikację użytkownika w kontrolerach WebSocket.

### WebSocket (websocket)

Folder websocket zawiera klasy związane z komunikacją WebSocket.

#### WebSocketConfig

WebSocketConfig konfiguruje WebSocket i STOMP, jak opisano wcześniej.

#### WebSocketAuthInterceptor

WebSocketAuthInterceptor autoryzuje połączenia WebSocket, jak opisano wcześniej.

#### ChatWebSocketController

ChatWebSocketController obsługuje wiadomości WebSocket związane z czatem. Kontroler używa adnotacji MessageMapping do mapowania wiadomości STOMP na metody. Metoda sendMessage obsługuje wysyłanie wiadomości czatu. Metoda odbiera wiadomość z destynacji /app/chat.send, zapisuje ją w bazie danych przy użyciu ChatService oraz wysyła do odbiorcy i nadawcy przez SimpMessagingTemplate. Metoda typing obsługuje wskaźnik pisania i wysyła go do odbiorcy. Metody setOnline i setOffline obsługują status online użytkownika oraz rozgłaszają zmiany statusu do znajomych.

### Powiadomienia (notification)

Folder notification zawiera klasy związane z powiadomieniami.

#### NotificationService

NotificationService obsługuje operacje związane z powiadomieniami, jak opisano wcześniej.

#### NotificationController

NotificationController obsługuje żądania HTTP związane z powiadomieniami, jak opisano wcześniej.

#### NotificationDTO

NotificationDTO reprezentuje powiadomienie w API, jak opisano wcześniej.

### Posty (post)

Folder post zawiera klasy związane z postami.

#### PostService

PostService obsługuje operacje związane z postami, jak opisano wcześniej.

#### PostController

PostController obsługuje żądania HTTP związane z postami, jak opisano wcześniej.

#### PostDTO i PostCreateRequest

PostDTO reprezentuje post w API, a PostCreateRequest jest używany do tworzenia nowych postów.

### Obsługa Wyjątków (exception)

Folder exception zawiera klasy związane z obsługą błędów.

#### GlobalExceptionHandler

GlobalExceptionHandler jest klasą oznaczoną adnotacją RestControllerAdvice, która przechwytuje wszystkie wyjątki w aplikacji i zwraca spójne odpowiedzi błędów. Handler obsługuje różne typy wyjątków, takie jak BadCredentialsException, UsernameNotFoundException, MethodArgumentNotValidException, LazyInitializationException oraz ogólne wyjątki. Każdy typ wyjątku jest mapowany na odpowiedni kod statusu HTTP oraz komunikat błędu. Handler zwraca obiekty ApiError, które zawierają informacje o błędzie w spójnym formacie.

## Implementacja WebSocket

WebSocket jest zaimplementowany przy użyciu Spring WebSocket oraz protokołu STOMP. Komunikacja w czasie rzeczywistym jest używana do dostarczania wiadomości czatu, powiadomień oraz aktualizacji statusu online.

### Konfiguracja WebSocket

WebSocketConfig konfiguruje message broker STOMP, który obsługuje tematy (/topic) oraz kolejki (/queue). Prefiks /app jest używany dla wiadomości wysyłanych przez klientów, a prefiks /user jest używany dla wiadomości wysyłanych do konkretnego użytkownika. Endpoint /ws jest zarejestrowany jako STOMP endpoint z obsługą SockJS dla kompatybilności z przeglądarkami.

### Autoryzacja WebSocket

WebSocketAuthInterceptor przechwytuje komendę CONNECT i weryfikuje token JWT z nagłówka Authorization. Token jest wyciągany z nagłówka, weryfikowany przy użyciu JwtService oraz jeśli jest ważny, Principal jest ustawiany w SecurityContext. Principal jest następnie dostępny w kontrolerach WebSocket przez parametr Principal w metodach obsługujących wiadomości.

### Obsługa Wiadomości

ChatWebSocketController obsługuje wiadomości WebSocket związane z czatem. Metoda sendMessage odbiera wiadomość z destynacji /app/chat.send, zapisuje ją w bazie danych oraz wysyła do odbiorcy i nadawcy przez SimpMessagingTemplate. Wiadomości są wysyłane do destynacji /queue/messages dla konkretnego użytkownika, używając username jako identyfikatora. Metoda typing obsługuje wskaźnik pisania i wysyła go do odbiorcy przez destynację /queue/typing. Metody setOnline i setOffline obsługują status online użytkownika oraz rozgłaszają zmiany statusu do znajomych przez destynację /queue/status.

### Routing Wiadomości

Spring WebSocket używa username z Principal do routingu wiadomości do konkretnych użytkowników. Metoda convertAndSendToUser w SimpMessagingTemplate używa username do wysłania wiadomości do konkretnego użytkownika. Wiadomości są automatycznie routowane do destynacji /user/{username}/queue/messages, gdzie username jest username użytkownika z Principal.

## Migracje Bazy Danych

Migracje bazy danych są zarządzane przez Flyway. Migracje są przechowywane w folderze resources/db/migration i są wykonywane automatycznie przy starcie aplikacji. Każda migracja ma numer wersji oraz opis, co pozwala na śledzenie zmian w schemacie bazy danych.

### V1__initial_schema.sql

Pierwsza migracja tworzy podstawowy schemat bazy danych, w tym tabele users, profiles, posts, comments, post_likes, user_friends, events, event_interested, event_participants, notifications, chat_messages oraz conversations. Migracja definiuje również klucze obce oraz indeksy dla optymalizacji zapytań.

### V2__seed_data.sql

Druga migracja dodaje dane testowe do bazy danych, w tym przykładowych użytkowników oraz innych danych potrzebnych do testowania aplikacji.

### V3__reset_passwords.sql

Trzecia migracja resetuje hasła użytkowników testowych do znanych wartości, co ułatwia testowanie.

### V4__fix_password_hashes.sql

Czwarta migracja naprawia hashe haseł, które mogą być nieprawidłowo zakodowane.

### V5__add_friend_request_notification_types.sql

Piąta migracja dodaje typy powiadomień związane z zaproszeniami do znajomych.

### V6__add_chat_notification_types.sql

Szósta migracja dodaje typy powiadomień związane z wiadomościami czatu.

### V7__add_interests.sql

Siódma migracja dodaje dostępne zainteresowania, które użytkownicy mogą wybrać podczas rejestracji profilu.

## Walidacja Danych

Aplikacja używa Bean Validation do walidacji danych wejściowych. Adnotacje takie jak NotBlank, Email, Size są używane w modelach oraz DTO do walidacji danych. GlobalExceptionHandler przechwytuje wyjątki MethodArgumentNotValidException i zwraca szczegółowe informacje o błędach walidacji w odpowiedzi.

## Paginacja

Aplikacja używa paginacji Spring Data JPA do zwracania dużych zbiorów danych. Endpointy takie jak /api/posts oraz /api/events zwracają dane z paginacją, co pozwala na efektywne ładowanie danych w częściach. Paginacja jest obsługiwana przez parametry page i size w żądaniach HTTP.

## Obsługa Plików

Aplikacja obsługuje przesyłanie plików, głównie avatary użytkowników oraz obrazy wydarzeń. Pliki są przesyłane jako multipart/form-data i są zapisywane w folderze uploads na serwerze. Ścieżka do pliku jest zwracana w odpowiedzi i jest używana przez frontend do wyświetlania obrazów.

## Transakcje

Aplikacja używa adnotacji Transactional do zarządzania transakcjami bazodanowymi. Operacje, które wymagają wielu zapytań do bazy danych, są oznaczone jako Transactional, co zapewnia atomicity operacji. Na przykład metoda acceptFriendRequest w UserService jest oznaczona jako Transactional, ponieważ wymaga wielu operacji na bazie danych.

## Lazy Loading

Aplikacja używa lazy loading dla relacji JPA, co oznacza, że powiązane encje są ładowane tylko wtedy, gdy są potrzebne. Lazy loading może powodować problemy z LazyInitializationException, jeśli encje są dostępne poza kontekstem transakcji. Aby uniknąć tego problemu, niektóre metody są oznaczone jako Transactional z readOnly=true, co zapewnia dostęp do powiązanych encji w kontekście transakcji.

## Logowanie

Aplikacja używa SLF4J oraz Logback do logowania. Poziomy logowania są konfigurowane w application.properties. Logowanie jest używane do debugowania oraz śledzenia operacji w aplikacji, szczególnie w kontekście WebSocket oraz autoryzacji.

## Testowanie

Aplikacja zawiera testy jednostkowe oraz integracyjne. Testy są napisane przy użyciu JUnit oraz Spring Boot Test. Testy WebSocket są napisane przy użyciu Spring WebSocket Test, co pozwala na testowanie komunikacji WebSocket bez uruchamiania pełnego serwera.

## Konfiguracja Aplikacji

Konfiguracja aplikacji jest przechowywana w pliku application.properties. Plik zawiera konfigurację bazy danych, JWT, CORS, uploadu plików oraz logowania. Konfiguracja może być nadpisana przez zmienne środowiskowe lub pliki konfiguracyjne dla różnych środowisk (development, production).

## Bezpieczeństwo

Aplikacja implementuje wiele mechanizmów bezpieczeństwa. Hasła są hashowane przy użyciu BCrypt przed zapisaniem w bazie danych. Tokeny JWT są podpisywane sekretnym kluczem i mają czas wygaśnięcia. WebSocket wymaga autoryzacji przez token JWT. CORS jest skonfigurowany, aby zezwalać tylko na żądania z określonych originów. CSRF jest wyłączony, ponieważ aplikacja używa JWT, który jest odporny na ataki CSRF.

## Podsumowanie

Backend Heyo jest kompleksowym serwerem API zbudowanym w Spring Boot z wykorzystaniem najnowszych technologii i najlepszych praktyk. Architektura oparta na kontrolerach, serwisach oraz repozytoriach zapewnia separację odpowiedzialności oraz łatwość utrzymania. Komunikacja w czasie rzeczywistym przez WebSocket oraz bezpieczeństwo oparte na JWT sprawiają, że aplikacja jest bezpieczna i responsywna. Migracje bazy danych przez Flyway oraz obsługa błędów przez GlobalExceptionHandler zapewniają niezawodność oraz łatwość wdrożenia.

