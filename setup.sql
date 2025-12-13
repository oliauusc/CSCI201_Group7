-- Create foodlocator database
CREATE DATABASE IF NOT EXISTS foodlocator;
USE foodlocator;

-- Users table (lowercase for compatibility with servlets)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Locations table
CREATE TABLE IF NOT EXISTS locations (
    locationID INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    lat DECIMAL(10, 8),
    lng DECIMAL(11, 8)
);

-- Reviews table
CREATE TABLE IF NOT EXISTS reviews (
    reviewID INT AUTO_INCREMENT PRIMARY KEY,
    locationID INT NOT NULL,
    userID INT NOT NULL,
    rating DECIMAL(2,1) CHECK (rating >= 0 AND rating <= 5),
    title VARCHAR(255) NOT NULL,
    body VARCHAR(2000) NOT NULL,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (locationID) REFERENCES locations(locationID),
    FOREIGN KEY (userID) REFERENCES users(id)
);

-- Insert sample locations
INSERT INTO locations (name, address, category, lat, lng) VALUES 
('Dulce', '3201 S Hoover St', 'Dessert', 34.0250, -118.2850),
('Everybody\'s Kitchen', '3201 S Hoover St', 'International', 34.0245, -118.2855),
('Lemonade', '929 W Jefferson Blvd', 'Healthy', 34.0265, -118.2845),
('Seeds Marketplace', '3201 S Hoover St', 'Cafe', 34.0230, -118.2865),
('Popchew', '3201 S Hoover St', 'Bubble Tea', 34.0240, -118.2840);

-- Insert test users
INSERT INTO users (email, username, password) VALUES 
('sarah@usc.edu', 'Sarah K.', 'password123'),
('mike@usc.edu', 'Mike T.', 'password123'),
('emma@usc.edu', 'Emma L.', 'password123'),
('john@usc.edu', 'John D.', 'password123'),
('lisa@usc.edu', 'Lisa W.', 'password123');

-- Insert sample reviews for Dulce (locationID = 1)
INSERT INTO reviews (locationID, userID, rating, title, body, createdAt) VALUES 
(1, 1, 4.0, 'My go-to spot', 'Absolutely love this place! The atmosphere is great and the food is even better.', NOW() - INTERVAL 7 DAY),
(1, 2, 4.5, 'Perfect desserts', 'The desserts here are amazing. My favorite is their chocolate cake. Highly recommend!', NOW() - INTERVAL 14 DAY),
(1, 3, 3.5, 'Good but pricey', 'Good quality desserts but a bit overpriced. Still worth trying though.', NOW() - INTERVAL 21 DAY),
(1, 4, 4.0, 'Great for dates', 'Perfect place to take someone special. The ambiance is romantic and the desserts are delicious.', NOW() - INTERVAL 30 DAY),
(1, 5, 4.5, 'Hidden gem', 'This has become my favorite spot on campus. The staff is friendly and everything is fresh.', NOW() - INTERVAL 5 DAY);

-- Insert sample reviews for Everybody's Kitchen (locationID = 2)
INSERT INTO reviews (locationID, userID, rating, title, body, createdAt) VALUES 
(2, 1, 4.0, 'Great variety', 'Love the international options here. Food is always fresh and well-prepared.', NOW() - INTERVAL 10 DAY),
(2, 3, 3.5, 'Good portions', 'The portions are generous and the food is tasty. A bit crowded during lunch though.', NOW() - INTERVAL 14 DAY),
(2, 2, 4.5, 'Best dining hall', 'This is hands down the best dining option on campus. I come here all the time.', NOW() - INTERVAL 4 DAY),
(2, 4, 4.0, 'Healthy choices', 'Great options for people watching their diet. Everything is nutritious and delicious.', NOW() - INTERVAL 12 DAY),
(2, 5, 3.5, 'Could use improvement', 'Food is decent but service could be faster. Wait times can be long during peak hours.', NOW() - INTERVAL 21 DAY);

-- Insert sample reviews for Lemonade (locationID = 3)
INSERT INTO reviews (locationID, userID, rating, title, body, createdAt) VALUES 
(3, 1, 4.5, 'Fresh and healthy', 'The salads here are incredible. Fresh ingredients and amazing combinations. Definitely worth the visit.', NOW() - INTERVAL 5 DAY),
(3, 2, 4.0, 'Perfect for quick lunch', 'Quick service and healthy food. Exactly what I need between classes.', NOW() - INTERVAL 7 DAY),
(3, 4, 4.5, 'Amazing juices', 'Their fresh juices are out of this world. I get one every time I visit.', NOW() - INTERVAL 14 DAY),
(3, 3, 3.5, 'A bit pricey', 'Good quality food but prices are on the higher side. Still worth it for the quality though.', NOW() - INTERVAL 21 DAY),
(3, 5, 4.0, 'Great vegan options', 'Excellent vegan menu. You don''t feel like you''re missing out on anything here.', NOW() - INTERVAL 10 DAY);

-- Insert sample reviews for Seeds Marketplace (locationID = 4)
INSERT INTO reviews (locationID, userID, rating, title, body, createdAt) VALUES 
(4, 2, 4.5, 'Best coffee on campus', 'Their coffee is seriously the best. Great atmosphere too. I work here all the time.', NOW() - INTERVAL 3 DAY),
(4, 1, 4.0, 'Solid sandwiches', 'The sandwiches are really good. Fresh ingredients and customizable options.', NOW() - INTERVAL 7 DAY),
(4, 5, 4.5, 'My daily stop', 'I come here every single day. Consistent quality and friendly staff.', NOW() - INTERVAL 2 DAY),
(4, 3, 4.0, 'Great for studying', 'Perfect spot to grab a coffee and study. Quiet and comfortable.', NOW() - INTERVAL 14 DAY),
(4, 4, 3.5, 'Lines can be long', 'Good quality but the lines get really long during peak hours. Plan accordingly.', NOW() - INTERVAL 28 DAY);

-- Insert sample reviews for Popchew (locationID = 5)
INSERT INTO reviews (locationID, userID, rating, title, body, createdAt) VALUES 
(5, 1, 4.5, 'Best bubble tea', 'Amazing bubble tea! The flavors are unique and the quality is top-notch.', NOW() - INTERVAL 6 DAY),
(5, 3, 4.0, 'Addicted to their teas', 'I''ve tried almost everything on the menu and it''s all delicious. Highly recommend!', NOW() - INTERVAL 7 DAY),
(5, 2, 4.5, 'Perfect spot with friends', 'Great place to hang out and enjoy bubble tea with friends. Chill vibe.', NOW() - INTERVAL 14 DAY),
(5, 4, 4.0, 'Good value', 'Reasonably priced and good quality. Worth every penny.', NOW() - INTERVAL 3 DAY),
(5, 5, 4.5, 'Must try', 'If you haven''t been here yet, you''re missing out. Seriously, go!', NOW() - INTERVAL 5 DAY);
