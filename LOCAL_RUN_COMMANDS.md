# Commands to Run Website Locally

## Step 1: Start Database (PostgreSQL)
```bash
cd infra
docker-compose up -d
```

## Step 2: Start Backend (Spring Boot)
```bash
cd vision_care_clinic/backend
mvn spring-boot:run
```

## Step 3: Start Frontend (React)
```bash
cd vision_care_clinic/frontend
npm install
npm run dev
```

---

## Complete Command Sequence

### Terminal 1 - Database:
```bash
cd infra
docker-compose up -d
```

### Terminal 2 - Backend:
```bash
cd vision_care_clinic/backend
mvn spring-boot:run
```

### Terminal 3 - Frontend:
```bash
cd vision_care_clinic/frontend
npm install
npm run dev
```

---

## Access URLs

- **Frontend:** http://localhost:5173
- **Backend API:** http://localhost:8080
- **Database:** localhost:5432

---

## Notes

- Run each command in a separate terminal window
- Make sure Docker is running before starting the database
- Backend must be running before frontend can connect
- First time setup: Run `npm install` in frontend directory (only needed once)

