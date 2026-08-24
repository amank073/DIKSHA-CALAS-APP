import requests

login_url = "http://localhost:8080/api/auth/login"
login_data = {
    "email": "varun@gmail.com",
    "password": "password123"
}
try:
    resp = requests.post(login_url, json=login_data)
    print("Login:", resp.status_code, resp.text)
    token = resp.json().get("token")
    if token:
        headers = {"Authorization": f"Bearer {token}"}
        active_plan_url = "http://localhost:8080/api/student/study-plans/active"
        resp2 = requests.get(active_plan_url, headers=headers)
        print("Active Plan:", resp2.status_code)
        if resp2.status_code != 200:
            print(resp2.text)
except Exception as e:
    print(e)
