# fullstack-react-springboot-docker

## Description
This project is a modern, scalable web application that leverages **React** for a dynamic frontend, **Spring Boot** for a robust backend, and **Spring Security** for comprehensive security measures. It utilizes **JWT** for authentication, **MySQL** for data persistence, and **Redis** for efficient session management. The project is containerized using **Docker Compose** and employs **GitHub Actions** for seamless CI/CD pipelines.

## Features
- Role-based user authentication and authorization using JWT and Spring Security
- Responsive and intuitive UI built with React and Material-UI
- RESTful API endpoints with Spring Boot
- Secure session management with Redis
- Automated CI/CD pipelines with GitHub Actions
- Containerized deployment using Docker Compose

## Installation
### 1. Get Docker & Docker Compose
- [Linux](https://docs.docker.com/engine/install/)
- [Windows](https://docs.docker.com/desktop/install/windows-install/)
- [Mac](https://docs.docker.com/desktop/install/mac-install/)

### 2. Clone the repository
```shell
git clone https://github.com/rogershi-dev/fullstack-react-springboot-docker.git

cd fullstack-react-springboot-docker/
```

### 3. Adjust ports
By default, these services run on the following ports:
- React: `http://localhost:3000`
- Spring Boot: `http://localhost:8082`
- MySQL: `http://localhost:3309`
- Redis: `http://localhost:6379`

Change the port number in `docker-compose.dev.yml` for any conflicts.

### 4. Build and run the containers
```shell
docker compose -f docker-compose.dev.yml up --build
```

### 5. Access the application
- React: `http://localhost:3000`
- Spring Boot: `http://localhost:8082`
- MySQL: `http://localhost:3309`
- Redis: `http://localhost:6379`

## Documentation
For a detailed, step-by-step setup guide, please visit my personal website: [Building a Modern Web Application: React, Spring Boot, Spring Security, and Docker Compose Explained](https://uniqueman.dev/showPostDetails/1731363525929).

## Usage
- 1.**System Initialization:** You'll be prompted to create an admin account the first time you start the application.
- 2.**Login:** Then you'll be redirected to the login page and you can log in directly using the admin account that you just created.
- 3.**Interact with the system:** Since this is a demo project, and I put a lot of the efforts on building the application structure, there aren't that many interactions for the time being. But you can create new user accounts.
- 4.**Register a new user:** Click the button on the home page, and you'll be able to register new users. 
- 5.**Logout:** Safely log out using the logout button to terminate your current session.
- 6.**Test role-based access control:** Log in using the new user account that you just created and you'll find out that you won't be able to create new users again, since that account is not an admin account.
- 7.**Test device limit:** The default device limit is 2 devices, try to log in on a third device(or browser), and refresh the page on the first device, you should be prompted for login since that session has been invalidated and blacklisted.


## Contributing
Contributions are welcome! I'm always open to discussions and improvements. If you find any issues or have suggestions, feel free to open an issue or submit a pull request.

```shell
# 1. Fork the repository

# 2. Create and switch to a new branch
git branch  feature/your-feature
git switch feature/your-feature

# 3. Stage and commit your changes
git add .
git commit -m "Added new feature"

# 4. Push to the branch
git push origin feature/your-feature

# 5. Open a pull request

```