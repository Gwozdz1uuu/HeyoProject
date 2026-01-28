# Heyo - Aplikacja Społecznościowa

## Opis Projektu

Heyo to aplikacja społecznościowa zbudowana w Angularze, która umożliwia użytkownikom tworzenie postów, komunikację w czasie rzeczywistym, zarządzanie znajomymi, organizowanie wydarzeń oraz otrzymywanie powiadomień. Aplikacja wykorzystuje architekturę SPA (Single Page Application) z komunikacją w czasie rzeczywistym poprzez WebSocket.

## Technologie i Narzędzia

### Framework i Biblioteki Główne

Aplikacja została zbudowana przy użyciu Angular 21, który jest frameworkiem opartym na TypeScript. Angular zapewnia strukturę komponentową, dependency injection, routing oraz reaktywność.

### Angular Material

Do budowy interfejsu użytkownika wykorzystano Angular Material, który dostarcza gotowe komponenty takie jak przyciski, ikony, spinnery, snackbary (powiadomienia), dialogi oraz formularze. Komponenty Material są używane w całej aplikacji do zapewnienia spójnego wyglądu.

### Reactive Forms

Do zarządzania formularzami użyto Reactive Forms z Angulara. Formularze są definiowane w komponentach TypeScript przy użyciu FormBuilder i FormGroup, co pozwala na walidację danych po stronie klienta oraz łatwe zarządzanie stanem formularzy.

### RxJS

RxJS jest używany do obsługi strumieni danych asynchronicznych. Wszystkie zapytania HTTP zwracają Observable, które są subskrybowane w komponentach. RxJS pozwala na transformację danych, obsługę błędów oraz zarządzanie subskrypcjami.

### WebSocket i STOMP

Do komunikacji w czasie rzeczywistym wykorzystano WebSocket z protokołem STOMP. Biblioteka @stomp/stompjs jest używana do zarządzania połączeniem WebSocket, a sockjs-client zapewnia fallback dla przeglądarek, które nie obsługują natywnie WebSocket.

### Bootstrap

Bootstrap 5 jest używany jako dodatkowa biblioteka CSS do niektórych komponentów interfejsu, głównie do przycisków i layoutu.

## Struktura Projektu

### Folder src/app

Główny folder aplikacji zawiera wszystkie komponenty, serwisy, modele oraz konfigurację routingu.

### Komponenty

Każdy komponent w Angularze składa się z trzech plików: TypeScript (.ts), HTML (.html) oraz CSS (.css). Komponenty są standalone, co oznacza, że każdy komponent jest niezależną jednostką z własnymi importami.

#### dashboard

Komponent dashboard wyświetla główny widok po zalogowaniu. Zawiera podsumowanie aktywności użytkownika, statystyki oraz szybki dostęp do głównych funkcji aplikacji.

#### events

Komponent events zarządza wydarzeniami. Umożliwia przeglądanie wydarzeń, tworzenie nowych oraz wyrażanie zainteresowania lub uczestnictwa. Zawiera również dialog do tworzenia wydarzeń (create-event-dialog), który jest osobnym komponentem wyświetlanym jako modalne okno dialogowe.

#### friends

Komponent friends obsługuje funkcjonalność znajomych. Umożliwia wyszukiwanie użytkowników, wysyłanie zaproszeń do znajomych, akceptowanie lub odrzucanie zaproszeń oraz przeglądanie listy znajomych. Komponent wykorzystuje wyszukiwanie w czasie rzeczywistym oraz wyświetla status online użytkowników.

#### home

Komponent home to główny feed aplikacji, gdzie użytkownicy widzą posty od znajomych oraz mogą tworzyć własne posty. Komponent obsługuje polubienia, komentarze oraz wyświetlanie obrazów w postach. Dodatkowo w feedzie są wstawiane reklamy między postami, które są renderowane jako osobne karty wyglądające jak posty.

#### layout

Komponent layout jest głównym kontenerem dla wszystkich chronionych tras. Zawiera sidebar nawigacyjny oraz router-outlet, gdzie są renderowane komponenty potomne. Layout jest odpowiedzialny za inicjalizację połączenia WebSocket oraz wyświetlanie statusu połączenia.

#### login

Komponent login obsługuje logowanie użytkowników. Używa Reactive Forms do walidacji danych wejściowych oraz wyświetla komunikaty błędów przy nieudanej autoryzacji.

#### messages

Komponent messages to sekcja czatów, która umożliwia komunikację w czasie rzeczywistym między użytkownikami. Komponent wykorzystuje WebSocket do natychmiastowego dostarczania wiadomości oraz wyświetlania wskaźnika pisania. Wiadomości są wyświetlane w formie konwersacji z podziałem na wiadomości własne i otrzymane.

#### notifications

Komponent notifications wyświetla powiadomienia użytkownika. Obsługuje różne typy powiadomień, w tym zaproszenia do znajomych, które można akceptować lub odrzucać bezpośrednio z poziomu powiadomienia. Powiadomienia są ładowane z serwera oraz mogą być aktualizowane w czasie rzeczywistym przez WebSocket.

#### profile

Komponent profile wyświetla profil użytkownika. Może wyświetlać zarówno własny profil użytkownika, jak i profil innego użytkownika (gdy jest przekazany parametr ID w routingu). Komponent obsługuje edycję avatara, zmianę username, wyświetlanie postów użytkownika oraz wydarzeń. W przypadku własnego profilu użytkownik może edytować dane, natomiast przy przeglądaniu profilu innego użytkownika opcje edycji są ukryte.

#### register i register-profile

Komponent register obsługuje podstawową rejestrację użytkownika (email i hasło). Po rejestracji użytkownik jest przekierowywany do register-profile, gdzie uzupełnia dane profilowe takie jak imię, nazwisko, nickname, avatar oraz zainteresowania. Rejestracja jest dwuetapowa - najpierw tworzone jest konto użytkownika, a następnie profil.

#### settings

Komponent settings umożliwia zarządzanie ustawieniami konta użytkownika.

#### sidenav

Komponent sidenav to boczny panel nawigacyjny, który jest widoczny we wszystkich chronionych widokach. Zawiera ikony i etykiety dla głównych sekcji aplikacji oraz przycisk "Więcej" z menu rozwijanym zawierającym opcje takie jak ustawienia, profil i wylogowanie.

### Serwisy (services)

Serwisy w Angularze są singletonami, które zawierają logikę biznesową oraz komunikację z backendem. Wszystkie serwisy są oznaczone jako providedIn root, co oznacza, że są dostępne w całej aplikacji.

#### auth.service

Serwis autoryzacji zarządza sesją użytkownika. Przechowuje token JWT w localStorage oraz informacje o użytkowniku. Zawiera metody do logowania, rejestracji, wylogowania oraz sprawdzania stanu autoryzacji. Serwis wykorzystuje Angular signals do reaktywnego zarządzania stanem użytkownika. Po udanym logowaniu automatycznie inicjalizuje połączenie WebSocket.

#### chat.service

Serwis czatu obsługuje komunikację z backendem dotyczącą konwersacji i wiadomości. Zawiera metody do pobierania listy konwersacji, wiadomości dla danej konwersacji oraz tworzenia nowych konwersacji. Serwis współpracuje z WebSocketService do otrzymywania wiadomości w czasie rzeczywistym.

#### event.service

Serwis wydarzeń zarządza operacjami związanymi z wydarzeniami. Umożliwia pobieranie listy wydarzeń, tworzenie nowych wydarzeń oraz wyrażanie zainteresowania lub uczestnictwa.

#### friends.service

Serwis znajomych obsługuje wszystkie operacje związane z znajomymi. Zawiera metody do wyszukiwania użytkowników, wysyłania zaproszeń, akceptowania i odrzucania zaproszeń oraz usuwania znajomych.

#### notification.service

Serwis powiadomień zarządza powiadomieniami użytkownika. Umożliwia pobieranie listy powiadomień, oznaczanie jako przeczytane oraz usuwanie powiadomień.

#### post.service

Serwis postów obsługuje operacje związane z postami. Zawiera metody do pobierania feedu, tworzenia postów, polubiania postów oraz zarządzania komentarzami. Wszystkie metody zwracają Observable, które są subskrybowane w komponentach.

#### profile.service

Serwis profilu zarządza danymi profilowymi użytkownika. Umożliwia pobieranie profilu, aktualizację danych profilowych, zmianę avatara oraz zmianę username. Serwis obsługuje również pobieranie dostępnych zainteresowań.

#### upload.service

Serwis uploadu obsługuje przesyłanie plików na serwer. Zawiera metodę do przesyłania avatara użytkownika. Pliki są przesyłane jako multipart/form-data.

#### websocket.service

Serwis WebSocket jest kluczowym elementem aplikacji odpowiedzialnym za komunikację w czasie rzeczywistym. Używa biblioteki @stomp/stompjs do zarządzania połączeniem STOMP przez WebSocket. Serwis implementuje wzorzec Observable do subskrybowania wiadomości z różnych destynacji. Zawiera metody do łączenia, rozłączania, subskrybowania tematów, wysyłania wiadomości oraz zarządzania stanem połączenia. Serwis automatycznie obsługuje ponowne łączenie przy utracie połączenia oraz zarządza subskrypcjami.

### Guards

Guards w Angularze są używane do kontroli dostępu do tras. W aplikacji jest jeden guard - auth.guard, który sprawdza, czy użytkownik jest zalogowany przed zezwoleniem na dostęp do chronionych tras. Jeśli użytkownik nie jest zalogowany, jest przekierowywany do strony logowania z parametrem returnUrl, który pozwala na powrót do żądanej strony po zalogowaniu.

### Interceptory

Interceptory w Angularze pozwalają na przechwytywanie i modyfikację żądań HTTP przed ich wysłaniem oraz odpowiedzi przed ich przetworzeniem. W aplikacji jest jeden interceptor - auth.interceptor, który automatycznie dodaje token JWT do nagłówka Authorization wszystkich żądań HTTP do API. Token jest pobierany z localStorage i dodawany do każdego żądania, co eliminuje potrzebę ręcznego dodawania tokenu w każdym serwisie.

### Modele (models)

Folder models zawiera interfejsy TypeScript definiujące struktury danych używane w aplikacji. Wszystkie modele są eksportowane z pliku index.ts, co ułatwia importowanie. Modele obejmują User, Post, Comment, Event, Notification, Conversation, ChatMessage oraz różne DTO (Data Transfer Objects) używane do komunikacji z backendem.

### Routing (app.routes.ts)

Routing w Angularze jest zdefiniowany w pliku app.routes.ts. Aplikacja używa child routes, gdzie LayoutComponent jest kontenerem dla wszystkich chronionych tras. Trasy publiczne (login, register) nie wymagają autoryzacji, natomiast wszystkie trasy wewnątrz LayoutComponent są chronione przez authGuard. Routing obsługuje również parametry dynamiczne, jak w przypadku profilu użytkownika (/profile/:id), gdzie ID jest przekazywane jako parametr routingu.

## Implementacja WebSocket

WebSocket jest zaimplementowany przy użyciu biblioteki @stomp/stompjs oraz sockjs-client. Połączenie jest inicjalizowane w AuthService po udanym logowaniu lub rejestracji. WebSocketService jest singletonem dostępnym w całej aplikacji.

### Proces Łączenia

Połączenie WebSocket jest nawiązywane przez wywołanie metody connect w WebSocketService. Metoda przyjmuje token JWT jako parametr, który jest używany do autoryzacji połączenia. Połączenie jest nawiązywane przez SockJS do endpointu /ws na serwerze. STOMP client jest konfigurowany z nagłówkami autoryzacji oraz callbackami dla różnych zdarzeń połączenia.

### Stan Połączenia

WebSocketService zarządza stanem połączenia przy użyciu BehaviorSubject, który emituje wartości enum WebSocketConnectionState. Stany obejmują CONNECTING, CONNECTED, DISCONNECTED oraz ERROR. Komponenty mogą subskrybować się na zmiany stanu połączenia, aby reagować na zmiany.

### Subskrypcje

Metoda subscribe w WebSocketService umożliwia subskrybowanie się do konkretnych destynacji (tematów lub kolejek STOMP). Subskrypcje są przechowywane w Map, co pozwala na łatwe zarządzanie wieloma subskrypcjami. Metoda zwraca Observable, który emituje wiadomości z danej destynacji. Wiadomości są parsowane z JSON i emitowane przez Subject, który jest filtrowany przez destynację.

### Wysyłanie Wiadomości

Metoda send w WebSocketService umożliwia wysyłanie wiadomości do konkretnej destynacji. Wiadomości są serializowane do JSON przed wysłaniem. Jeśli połączenie nie jest jeszcze nawiązane, wiadomość jest kolejkowana i wysłana po nawiązaniu połączenia.

### Obsługa Błędów i Ponowne Łączenie

WebSocketService zawiera logikę obsługi błędów oraz automatycznego ponownego łączenia. Przy utracie połączenia serwis próbuje ponownie nawiązać połączenie z opóźnieniem, które zwiększa się z każdą próbą. Maksymalna liczba prób ponownego połączenia jest ograniczona, aby uniknąć nieskończonych prób.

### Użycie w Komponentach

Komponenty używają WebSocketService do subskrybowania się na wiadomości oraz wysyłania wiadomości. Na przykład komponent messages subskrybuje się na wiadomości czatu dla konkretnej konwersacji oraz wysyła wiadomości przez WebSocket. Komponent notifications może subskrybować się na powiadomienia w czasie rzeczywistym.

## Zarządzanie Stanem

Aplikacja wykorzystuje Angular signals do reaktywnego zarządzania stanem. Signals są używane w serwisach do przechowywania stanu, który może być odczytywany przez komponenty. Na przykład AuthService używa signal do przechowywania aktualnego użytkownika, co pozwala komponentom na reaktywne reagowanie na zmiany stanu autoryzacji.

## Obsługa Formularzy

Aplikacja używa Reactive Forms do wszystkich formularzy. Formularze są definiowane w komponentach przy użyciu FormBuilder, który tworzy FormGroup z FormControls. Każdy FormControl może mieć walidatory, które sprawdzają poprawność danych przed wysłaniem. Komponenty wyświetlają komunikaty błędów na podstawie stanu walidacji formularza.

## Obsługa Błędów

Błędy HTTP są obsługiwane w komponentach poprzez operator catchError z RxJS. Komponenty wyświetlają komunikaty błędów użytkownikowi przy użyciu MatSnackBar z Angular Material. Błędy są również logowane do konsoli dla celów debugowania.

## Komunikacja z Backendem

Wszystkie zapytania HTTP są wykonywane przez HttpClient z Angulara. Serwisy zawierają metody, które zwracają Observable z odpowiedziami z API. URL endpointów są definiowane w pliku environment.ts, co pozwala na łatwą zmianę adresu serwera w zależności od środowiska (development, production).

## Bezpieczeństwo

Token JWT jest przechowywany w localStorage przeglądarki. Token jest automatycznie dodawany do wszystkich żądań HTTP przez authInterceptor. AuthGuard chroni wszystkie chronione trasy przed nieautoryzowanym dostępem. WebSocket również wymaga tokenu JWT do nawiązania połączenia.

## Responsywność

Aplikacja jest responsywna i dostosowuje się do różnych rozmiarów ekranu. Media queries w CSS są używane do zmiany layoutu na mniejszych ekranach. Na przykład na ekranach mniejszych niż 900px sidebar może być ukryty lub zmieniony na menu mobilne.

## Testowanie

Aplikacja używa Vitest jako frameworku testowego. Komponenty mogą być testowane przy użyciu Angular Testing Utilities. Pliki spec.ts zawierają przykładowe testy dla komponentów.

## Build i Deployment

Aplikacja może być zbudowana przy użyciu komendy ng build, która generuje zoptymalizowane pliki produkcyjne w folderze dist. Pliki mogą być następnie wdrożone na serwerze webowym. Konfiguracja build jest zdefiniowana w pliku angular.json.

## Najważniejsze Funkcjonalności

### Feed z Postami

Komponent home wyświetla feed z postami od znajomych użytkownika. Posty mogą zawierać tekst oraz obrazy. Użytkownicy mogą polubić posty oraz dodawać komentarze. Komentarze są ładowane na żądanie po kliknięciu przycisku "Skomentuj". W feedzie są również wstawiane reklamy między postami, które są renderowane jako osobne karty.

### Czat w Czasie Rzeczywistym

Komponent messages umożliwia komunikację w czasie rzeczywistym między użytkownikami. Wiadomości są dostarczane natychmiastowo przez WebSocket. Komponent wyświetla wskaźnik pisania, gdy użytkownik pisze wiadomość. Wiadomości są wyświetlane w formie konwersacji z podziałem na wiadomości własne (po prawej) i otrzymane (po lewej).

### Powiadomienia

Komponent notifications wyświetla powiadomienia użytkownika. Powiadomienia mogą być różnych typów, w tym zaproszenia do znajomych. Użytkownik może akceptować lub odrzucać zaproszenia bezpośrednio z poziomu powiadomienia. Powiadomienia mogą być oznaczane jako przeczytane.

### Zarządzanie Znajomymi

Komponent friends umożliwia wyszukiwanie użytkowników, wysyłanie zaproszeń do znajomych oraz zarządzanie listą znajomych. Komponent wyświetla status online znajomych oraz umożliwia przeglądanie profilu znajomego.

### Wydarzenia

Komponent events umożliwia przeglądanie wydarzeń, tworzenie nowych wydarzeń oraz wyrażanie zainteresowania lub uczestnictwa. Wydarzenia mogą zawierać obraz, opis, datę oraz lokalizację.

### Profil Użytkownika

Komponent profile wyświetla profil użytkownika z możliwością edycji własnego profilu lub przeglądania profilu innego użytkownika. Komponent obsługuje zmianę avatara, zmianę username oraz wyświetlanie postów i wydarzeń użytkownika.

## Podsumowanie

Aplikacja Heyo jest kompleksową aplikacją społecznościową zbudowaną w Angularze z wykorzystaniem najnowszych technologii i najlepszych praktyk. Architektura oparta na komponentach, serwisach oraz reaktywnych strumieniach danych zapewnia skalowalność i łatwość utrzymania. Komunikacja w czasie rzeczywistym przez WebSocket oraz zarządzanie stanem przez signals sprawiają, że aplikacja jest responsywna i interaktywna.
