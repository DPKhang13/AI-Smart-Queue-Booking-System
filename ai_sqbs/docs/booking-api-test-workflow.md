# Booking API Test Workflow

Base URL:

```text
http://localhost:8080
```

Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

## 1. Rule Nghiep Vu Can Nho

Booking workflow hien tai:

```text
Create booking -> CONFIRMED
CONFIRMED -> CANCELLED
CONFIRMED -> COMPLETED
CONFIRMED -> NO_SHOW
```

Luu y:

- API create booking hien tai tao booking voi status `CONFIRMED`, khong tao `PENDING`.
- API `confirm` chi hop ly neu sau nay co flow tao booking `PENDING`, hoac ban insert booking `PENDING` thu cong trong DB de test.
- Khong dung API update status tu do. Moi action co endpoint rieng: `cancel`, `complete`, `no-show`, `confirm`.
- `USER` chi xem/huy booking cua chinh minh.
- `ADMIN` xem/huy booking bat ky va duoc `complete`, `no-show`, `confirm`.

## 2. Tai Khoan Test

Neu DB local da co seed data, co the dung admin ban dang test:

```json
{
  "usernameOrEmail": "admin@smartqueue.local",
  "password": "your-admin-password"
}
```

Neu chua co user thuong, tao user bang register:

```http
POST /api/auth/register
```

```json
{
  "fullName": "Test Customer",
  "email": "customer.booking.test@smartqueue.local",
  "username": "customer_booking_test",
  "phone": "0900000001",
  "password": "Password123"
}
```

Sau do can verify email OTP truoc khi user tao booking. Neu DB local dang test nhanh, co the update truc tiep:

```sql
UPDATE users
SET email_verified = TRUE,
    email_verified_at = NOW(),
    is_active = TRUE,
    is_deleted = FALSE
WHERE email = 'customer.booking.test@smartqueue.local';
```

Login user:

```http
POST /api/auth/login
```

```json
{
  "usernameOrEmail": "customer.booking.test@smartqueue.local",
  "password": "Password123"
}
```

Copy `accessToken` roi bam nut `Authorize` tren Swagger:

```text
Bearer <accessToken>
```

## 3. Chuan Bi Master Data

Neu DB da co branch/service type/capacity slot hop le, co the bo qua buoc tao moi va dung ID co san.

Khuyen nghi test voi ngay:

```text
bookingDate: 2026-07-06
bookingTime: 09:00:00
```

Ngay `2026-07-06` la thu Hai, nen `dayOfWeek = 1`.

### 3.1. Tao Branch

Role: `ADMIN`

```http
POST /api/branches/create
```

```json
{
  "name": "Booking Test Branch",
  "address": "123 Booking Test Street, Ho Chi Minh City",
  "phone": "02899990001",
  "defaultOpeningTime": "08:00:00",
  "defaultClosingTime": "17:00:00",
  "maxQueueCapacity": 100,
  "averageServiceDuration": 20
}
```

Expected:

```text
201 Created
```

Lay `branchId` trong response.

### 3.2. Tao Service Type

Role: `ADMIN`

```http
POST /api/branches/create/{branchId}/service-types
```

Vi du neu `branchId = 1`:

```http
POST /api/branches/create/1/service-types
```

```json
{
  "name": "Booking Test Service",
  "description": "Service used for booking workflow test",
  "estimatedDurationMinutes": 20
}
```

Expected:

```text
201 Created
```

Lay `serviceTypeId` trong response.

### 3.3. Tao Branch Schedule Cho Thu Hai

Role: `ADMIN`

```http
POST /api/branch-schedules/create/{branchId}/schedules
```

```json
{
  "dayOfWeek": 1,
  "openingTime": "08:00:00",
  "closingTime": "17:00:00",
  "isClosed": false
}
```

Expected:

```text
201 Created
```

Neu branch chua co schedule, booking se fallback ve `defaultOpeningTime/defaultClosingTime`, nen buoc nay khong bat buoc. Nhung tao schedule giup test ro rang hon.

### 3.4. Tao Capacity Slot

Role: `ADMIN`

```http
POST /api/service-capacity-slots/create
```

```json
{
  "branchId": 1,
  "serviceTypeId": 1,
  "dayOfWeek": 1,
  "specificDate": null,
  "startTime": "09:00:00",
  "endTime": "10:00:00",
  "maxBookings": 2,
  "maxQueueTickets": 10
}
```

Expected:

```text
201 Created
```

Luu y:

- Chi truyen mot trong hai field: `dayOfWeek` hoac `specificDate`.
- Neu dung `specificDate`, body mau:

```json
{
  "branchId": 1,
  "serviceTypeId": 1,
  "dayOfWeek": null,
  "specificDate": "2026-07-06",
  "startTime": "09:00:00",
  "endTime": "10:00:00",
  "maxBookings": 2,
  "maxQueueTickets": 10
}
```

## 4. Flow A: User Tao Booking Roi Tu Huy

### 4.1. User Tao Booking

Role: `USER` hoac `ADMIN`

```http
POST /api/bookings/create
```

```json
{
  "branchId": 1,
  "serviceTypeId": 1,
  "bookingDate": "2026-07-06",
  "bookingTime": "09:00:00",
  "note": "Customer wants a morning booking"
}
```

Expected:

```text
201 Created
status = CONFIRMED
```

Response mau:

```json
{
  "bookingId": 1,
  "bookingCode": "BK-20260706-ABC123",
  "userId": 8,
  "userFullName": "Test Customer",
  "branchId": 1,
  "branchName": "Booking Test Branch",
  "serviceTypeId": 1,
  "serviceTypeName": "Booking Test Service",
  "bookingDate": "2026-07-06",
  "bookingTime": "09:00:00",
  "status": "CONFIRMED",
  "note": "Customer wants a morning booking",
  "cancelledAt": null,
  "cancellationReason": null,
  "archivedAt": null
}
```

Lay `bookingId` de test cac buoc sau.

### 4.2. User Xem Danh Sach Booking Cua Minh

Role: `USER`

```http
GET /api/bookings/get-my
```

Expected:

```text
200 OK
```

### 4.3. User Xem Booking Theo ID

Role: `USER` owner hoac `ADMIN`

```http
GET /api/bookings/getId/{bookingId}
```

Vi du:

```http
GET /api/bookings/getId/1
```

Expected:

```text
200 OK
```

### 4.4. User Huy Booking Cua Minh

Role: `USER` owner hoac `ADMIN`

```http
PATCH /api/bookings/{bookingId}/cancel
```

```json
{
  "cancellationReason": "Customer changed schedule"
}
```

Expected:

```text
200 OK
status = CANCELLED
cancelledAt != null
cancellationReason = Customer changed schedule
```

### 4.5. Test Loi Sau Khi Da Cancel

Thu complete booking da cancel:

```http
PATCH /api/bookings/{bookingId}/complete
```

Expected:

```text
400/409 style error
BOOKING_STATUS_TRANSITION_INVALID
```

## 5. Flow B: Admin Complete Booking

Tao booking moi bang user voi gio khac de khong trung duplicate:

```http
POST /api/bookings/create
```

```json
{
  "branchId": 1,
  "serviceTypeId": 1,
  "bookingDate": "2026-07-06",
  "bookingTime": "09:20:00",
  "note": "Booking to be completed by admin"
}
```

Expected:

```text
201 Created
status = CONFIRMED
```

Sau do login admin, authorize bang admin token.

Admin complete:

```http
PATCH /api/bookings/{bookingId}/complete
```

Khong can request body.

Expected:

```text
200 OK
status = COMPLETED
```

## 6. Flow C: Admin Mark No Show

Tao booking moi bang user voi gio khac:

```http
POST /api/bookings/create
```

```json
{
  "branchId": 1,
  "serviceTypeId": 1,
  "bookingDate": "2026-07-06",
  "bookingTime": "09:40:00",
  "note": "Booking to be marked no-show by admin"
}
```

Admin mark no-show:

```http
PATCH /api/bookings/{bookingId}/no-show
```

Khong can request body.

Expected:

```text
200 OK
status = NO_SHOW
```

## 7. Flow D: Confirm Endpoint

Voi code hien tai, booking tao moi da la `CONFIRMED`, nen goi:

```http
PATCH /api/bookings/{bookingId}/confirm
```

tren booking vua tao se khong hop le, vi transition `CONFIRMED -> CONFIRMED` khong duoc cho phep.

Expected neu goi confirm tren booking da `CONFIRMED`:

```text
400/409 style error
BOOKING_STATUS_TRANSITION_INVALID
```

Chi test `confirm` thanh cong khi co booking `PENDING`. Neu can test thu cong, insert/update DB local:

```sql
UPDATE bookings
SET status = 'PENDING'
WHERE booking_id = 1;
```

Sau do login admin va goi:

```http
PATCH /api/bookings/1/confirm
```

Expected:

```text
200 OK
status = CONFIRMED
```

## 8. Negative Test Cases Nen Thu

### 8.1. Chua Login Tao Booking

```http
POST /api/bookings/create
```

Expected:

```text
401 Unauthorized
```

### 8.2. User Khac Xem Booking Khong Phai Cua Minh

Login bang user B:

```http
GET /api/bookings/getId/{bookingId_cua_user_A}
```

Expected:

```text
403 Forbidden
BOOKING_ACCESS_DENIED
```

### 8.3. Tao Booking Trung Slot Cua Cung User

Goi lai body create y chang booking dang `CONFIRMED`:

```json
{
  "branchId": 1,
  "serviceTypeId": 1,
  "bookingDate": "2026-07-06",
  "bookingTime": "09:00:00",
  "note": "Duplicate booking test"
}
```

Expected:

```text
BOOKING_ALREADY_EXISTS
```

Neu booking cu da `CANCELLED`, `COMPLETED`, hoac `NO_SHOW` thi slot do khong con bi chan theo unique active booking rule.

### 8.4. Vuot Capacity Slot

Neu capacity slot:

```text
09:00:00 -> 10:00:00
maxBookings = 2
```

Tao 3 booking active trong cung slot thoi gian `09:00`, `09:20`, `09:40` cho cung branch/service/date.

Expected booking thu 3:

```text
BOOKING_SLOT_FULL
```

### 8.5. Booking Ngoai Gio Mo Cua

```json
{
  "branchId": 1,
  "serviceTypeId": 1,
  "bookingDate": "2026-07-06",
  "bookingTime": "18:00:00",
  "note": "Outside opening hours"
}
```

Expected:

```text
BOOKING_TIME_INVALID
```

### 8.6. Booking Ngay Qua Khu

```json
{
  "branchId": 1,
  "serviceTypeId": 1,
  "bookingDate": "2026-06-01",
  "bookingTime": "09:00:00",
  "note": "Past date"
}
```

Expected:

```text
BOOKING_DATE_INVALID
```

### 8.7. Service Type Khong Thuoc Branch

Dung `branchId` cua branch A nhung `serviceTypeId` cua branch B.

Expected:

```text
SERVICE_TYPE_NOT_BELONG_TO_BRANCH
```

## 9. Thu Tu Test Nhanh Tren Swagger

1. Login admin.
2. Tao branch.
3. Tao service type theo branch.
4. Tao schedule thu Hai.
5. Tao capacity slot thu Hai luc `09:00:00 -> 10:00:00`.
6. Login user.
7. Tao booking luc `09:00:00`, expect `CONFIRMED`.
8. GET `/api/bookings/get-my`.
9. GET `/api/bookings/getId/{bookingId}`.
10. PATCH cancel booking, expect `CANCELLED`.
11. Tao booking moi luc `09:20:00`.
12. Login admin.
13. PATCH complete booking moi, expect `COMPLETED`.
14. Tao booking moi luc `09:40:00`.
15. Login admin.
16. PATCH no-show booking moi, expect `NO_SHOW`.

## 10. Body Mau De Copy Nhanh

Create booking:

```json
{
  "branchId": 1,
  "serviceTypeId": 1,
  "bookingDate": "2026-07-06",
  "bookingTime": "09:00:00",
  "note": "Swagger booking workflow test"
}
```

Cancel booking:

```json
{
  "cancellationReason": "Customer requested cancellation"
}
```

Admin action khong can body:

```text
PATCH /api/bookings/{bookingId}/complete
PATCH /api/bookings/{bookingId}/no-show
PATCH /api/bookings/{bookingId}/confirm
```
