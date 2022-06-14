# AccountService Introduction

The backend project's requirement is from Jetbrains Academy (links: https://hyperskill.org/projects/217)

Tech : Spring Security + Spring JPA + MySQL


# Description of each file 
Build (gradle)

Resource folder contains
  1. properties 
  2. keystore for https (demos purpose)

account folder contains the main code
  1. main
    - AccountServiceApplication
  2. controller 
  - signUpController
  - AuthenticatedUsersController
  3. repos
  - UserRepository
  - paymentsRepository
  - SecurityeventsList
  4. model
  - User
  - payments
  - Securityevents
  - UserDetailsImpl
  5. security
  - CustomAccessDeniedHandler
  - RestAuthenticationEntryPoint
  - WebSecurityConfigurerImpl
  6. Service
  - UserDetailsServiceImpl 
