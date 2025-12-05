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
