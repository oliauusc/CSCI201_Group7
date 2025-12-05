CREATE DATABASE IF NOT EXISTS foodLocator;
USE foodLocator;

-- Users
CREATE TABLE Users (
    userID INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- Locations
CREATE TABLE Locations (
    locationID INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL
);

-- Reviews, connects to users and locations
CREATE TABLE Reviews (
    reviewID INT AUTO_INCREMENT PRIMARY KEY,
    locationID INT NOT NULL,
    userID INT NOT NULL,
    rating DECIMAL(2,1) CHECK (rating >= 0 AND rating <= 5),
    title VARCHAR(255) NOT NULL,
    body VARCHAR(2000) NOT NULL,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (locationID) REFERENCES Locations(locationID),
    FOREIGN KEY (userID) REFERENCES Users(userID)
);

-- Reviews and users will be inserted through code after created
-- Prepopulate food locations 
-- for future reference & consistency, addresses come from apple maps
INSERT INTO Locations (name, address, category)
VALUES 
('Cava', '3201 S Hoover St, Ste 1840', 'Mediterranean'),
('IL Giardino', '3201 S Hoover St', 'Italian'),
('KOBUNGA', '929 W Jefferson Blvd', 'Korean'),
('Bruxie', '3201 S Hoover St, Ste 1845', 'Chicken Restaurant'),
('Ramen Kenjo', '929 W Jefferson Blvd, Ste 1630', 'Ramen Restaurant'),
('Insomnia Cookies', '929 W Jefferson Blvd, Ste 1620', 'Dessert'),
('Sunlife Organics', '929 W Jefferson Blvd', 'Smoothie Bar'),
('sweetgreen', '929 W Jefferson Blvd', 'Salad Bar'),
('Starbucks', '3201 S Hoover St', 'Cafe'),
('Jimmy John\'s', '3201 S Hoover St, Ste 1835', 'Sandwiches'),
('USC Village Dining Hall', '3096 McClintock Ave', 'Cafeteria'),
('Galen Dining Hall', 'Watt Way', 'Cafeteria'),
('Panda Express', '3607 Trousdale Pkwy', 'Chinese'),
('Moreton Fig', '3607 Trousdale Pkwy', 'New American'),
('Cafe Dulce', '3096 McClintock Ave Ste 1420', 'Cafe');











