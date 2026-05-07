import sys
import os
from PySide6.QtWidgets import QApplication, QMainWindow
from PySide6.QtWebEngineWidgets import QWebEngineView
from PySide6.QtWebEngineCore import QWebEnginePage
from PySide6.QtCore import QUrl, QTimer

class WebEnginePage(QWebEnginePage):
    def javaScriptConsoleMessage(self, level, message, lineNumber, sourceID):
        print(f"JS Console [{level}]: {message} (Line {lineNumber})")

app = QApplication(sys.argv)
view = QWebEngineView()
page = WebEnginePage()
view.setPage(page)

base_dir = os.path.dirname(os.path.abspath(__file__))
map_url = QUrl.fromLocalFile(os.path.join(base_dir, "resources", "map.html"))
print(f"Loading URL: {map_url.toString()}")

view.load(map_url)

# Stop after 5 seconds
QTimer.singleShot(5000, app.quit)
app.exec()
print("Test completed.")
