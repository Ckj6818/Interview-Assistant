"""Capture authenticated screenshots from the running local app."""
from pathlib import Path
import time

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.edge.options import Options

OUT_DIR = Path(__file__).resolve().parents[1] / "docs" / "screenshots"
BASE_URL = "http://127.0.0.1:8081"

OUT_DIR.mkdir(parents=True, exist_ok=True)

options = Options()
options.add_argument("--headless=new")
options.add_argument("--window-size=1440,900")
options.add_argument("--disable-gpu")

driver = webdriver.Edge(options=options)
driver.set_window_size(1440, 900)

try:
    driver.get(f"{BASE_URL}/login")
    time.sleep(1)
    driver.find_element(By.ID, "username").send_keys("user")
    driver.find_element(By.ID, "password").send_keys("123456")
    driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()
    time.sleep(2)

    driver.save_screenshot(str(OUT_DIR / "01-home.png"))

    driver.get(f"{BASE_URL}/questions")
    time.sleep(2)
    driver.save_screenshot(str(OUT_DIR / "02-questions.png"))

    driver.get(f"{BASE_URL}/mock-interview")
    time.sleep(3)
    driver.save_screenshot(str(OUT_DIR / "03-mock-interview.png"))

    print("Screenshots saved to docs/screenshots/")
finally:
    driver.quit()
