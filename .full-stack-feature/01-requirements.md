# Requirements: FightSphere AI — Module 5

## Problem Statement
Module 5 is the Fan Engagement & Community layer of FightSphere AI. It provides combat sports fans with tools to interact with the platform: book event tickets, make fight predictions, react to fight results, read blog/news content, receive notifications, and set preferences. Admins manage all content and can moderate the community.

## Acceptance Criteria
- [ ] Fan can browse and read published blog articles, filter by category, search by keyword
- [ ] Admin can create, edit, publish, archive, and delete blog articles
- [ ] Fan can book event tickets (VIP/REGULAR/STANDING), get QR code confirmation
- [ ] Fan can cancel own bookings; Admin can cancel any booking
- [ ] Admin sees booking stats (total, confirmed, cancelled, revenue)
- [ ] Fan can submit fight predictions (winner + method) per match
- [ ] Predictions lock when event goes live; Admin can score predictions
- [ ] Leaderboard shows fan prediction rankings by season and event
- [ ] Fan can post one reaction per fight result (emoji type + comment)
- [ ] Admin can pin/delete
- reactions on the reaction wall
- [ ] Fan receives notifications; can mark read/all-read
- [ ] Admin can broadcast notifications to all fans or by discipline
- [ ] Fan can set favorite discipline and fighter preferences
- [ ] App launches with role picker (Admin=id1 / Fan=id6), no real auth

## Scope

### In Scope
- 6 owned DB tables: blog_article, blog_category, event_booking, fan_prediction, fan_reaction, fan_notification, fan_preference
- 6 READ-ONLY tables from other modules: user, event, fighter, match_proposal, fight_result, discipline, venue
- Full CRUD for all 6 owned features
- QR code generation via ZXing
- UFC/ONE Championship-inspired dark UI with animations (JavaFX CSS)
- Role switcher (no real authentication)

### Out of Scope
- Authentication/login system (another module)
- Event management (Module 1)
- Fighter rankings (Module 2)
- Match proposals (Module 3)
- Fight results posting (Module 4)

## Technical Constraints
- Java 17, JavaFX 21.0.2, MySQL connector 8.3.0 (MariaDB 10.4 locally)
- ZXing 3.5.3 for QR
- DB: smartfight @ localhost:3306, root / (no password)
- GroupId: tn.smartfight, ArtifactId: smartfight-fan
- Main class: tn.smartfight.Main
- Window: 1280x800, min 1024x700

## Technology Stack
- Frontend: JavaFX 21 FXML + CSS (UFC dark theme)
- Backend: Java 17 service layer
- Database: MySQL/MariaDB via JDBC (no ORM)
- Build: Maven

## Dependencies
- Reads from: Module 1 (event), Module 2 (fighter), Module 3 (match_proposal), Module 4 (fight_result)
- When real auth integrates: other modules call SessionManager.login(user)

## Configuration
- Stack: JavaFX desktop
- API Style: none (direct JDBC service layer)
- Complexity: complex (61 files: 45 Java + 16 FXML)