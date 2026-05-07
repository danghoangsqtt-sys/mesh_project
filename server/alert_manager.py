"""
Alert Manager for Soldier Tracking System

Handles configurable health and system alerts based on thresholds
defined in alerts.conf
"""

import configparser
from enum import IntEnum
from typing import Dict, List, Tuple


class AlertLevel(IntEnum):
    """Alert severity levels"""
    OK = 0
    WARNING = 1
    CRITICAL = 2
    EMERGENCY = 3


class AlertType:
    """Alert type constants"""
    HEART_RATE_LOW = "heart_rate_low"
    HEART_RATE_HIGH = "heart_rate_high"
    SPO2_LOW = "spo2_low"
    TEMPERATURE_LOW = "temperature_low"
    TEMPERATURE_HIGH = "temperature_high"
    HUMIDITY_LOW = "humidity_low"
    HUMIDITY_HIGH = "humidity_high"
    PRESSURE_LOW = "pressure_low"
    PRESSURE_HIGH = "pressure_high"
    BATTERY_LOW = "battery_low"
    BATTERY_CRITICAL = "battery_critical"
    MAN_DOWN = "man_down"
    HEAT_STRESS = "heat_stress"
    GPS_INVALID = "gps_invalid"
    OFFLINE = "offline"


class AlertManager:
    """Manages alert thresholds and evaluates soldier health status"""
    
    def __init__(self, config_file='alerts.conf'):
        self.config = configparser.ConfigParser()
        self.config.read(config_file)
        
        # Load thresholds
        self._load_thresholds()
    
    def _load_thresholds(self):
        """Load alert thresholds from config file"""
        health = self.config['health_alerts']
        system = self.config['system_alerts']
        levels = self.config['alert_levels']
        
        # Heart rate thresholds
        self.hr_min = int(health.get('heart_rate_min', 40))
        self.hr_max = int(health.get('heart_rate_max', 120))
        self.hr_critical_min = int(health.get('heart_rate_critical_min', 30))
        self.hr_critical_max = int(health.get('heart_rate_critical_max', 150))
        
        # SpO2 thresholds
        self.spo2_min = int(health.get('spo2_min', 90))
        self.spo2_critical_min = int(health.get('spo2_critical_min', 80))
        
        # Temperature thresholds (Celsius)
        self.temp_min = float(health.get('temperature_min', 35.0))
        self.temp_max = float(health.get('temperature_max', 38.5))
        self.temp_critical_min = float(health.get('temperature_critical_min', 34.0))
        self.temp_critical_max = float(health.get('temperature_critical_max', 40.0))
        
        # Humidity thresholds (%)
        self.humidity_min = float(health.get('humidity_min', 15.0))
        self.humidity_max = float(health.get('humidity_max', 80.0))
        self.humidity_critical_min = float(health.get('humidity_critical_min', 10.0))
        self.humidity_critical_max = float(health.get('humidity_critical_max', 90.0))

        # Pressure thresholds (hPa)
        self.pressure_min = float(health.get('pressure_min', 980.0))
        self.pressure_max = float(health.get('pressure_max', 1050.0))
        self.pressure_critical_min = float(health.get('pressure_critical_min', 960.0))
        self.pressure_critical_max = float(health.get('pressure_critical_max', 1065.0))

        # Battery thresholds (Volts)
        self.battery_low = float(health.get('battery_low', 3.5))
        self.battery_critical = float(health.get('battery_critical', 3.3))
        
        # System thresholds
        self.man_down_threshold = float(system.get('man_down_threshold', 20.0))
        self.comm_timeout = int(system.get('comm_timeout', 30))
        self.gps_timeout = int(system.get('gps_timeout', 60))
        
        # Alert levels
        self.heat_stress_warning = int(levels.get('heat_stress_warning', 1))
        self.heat_stress_critical = int(levels.get('heat_stress_critical', 2))
        self.man_down_level = int(levels.get('man_down_level', 3))
        self.battery_low_level = int(levels.get('battery_low_level', 1))
        self.battery_critical_level = int(levels.get('battery_critical_level', 2))
        self.offline_level = int(levels.get('offline_level', 2))
    
    def evaluate_soldier(self, soldier_data: Dict) -> Tuple[AlertLevel, List[str]]:
        """
        Evaluate soldier health and return alert level and active alerts
        
        Args:
            soldier_data: Dictionary containing soldier health metrics
                         Keys: heartRate, spo2, temperature, batteryVoltage,
                               manDown, heatStress, online
        
        Returns:
            (alert_level, list_of_alerts)
        """
        alerts = []
        max_level = AlertLevel.OK
        
        # Check if soldier is online
        if not soldier_data.get('online', False):
            alerts.append({
                'type': AlertType.OFFLINE,
                'level': self.offline_level,
                'message': 'Soldier offline'
            })
            max_level = max(max_level, self.offline_level)
        
        # Check man down flag
        if soldier_data.get('manDown', False):
            alerts.append({
                'type': AlertType.MAN_DOWN,
                'level': self.man_down_level,
                'message': 'MAN DOWN - Immediate assistance required!'
            })
            max_level = max(max_level, self.man_down_level)
        
        # Check heart rate (key is 'heartRate' in camelCase)
        hr = soldier_data.get('heartRate', 0)
        if hr > 0:  # Only check if we have valid data
            if hr < self.hr_critical_min or hr > self.hr_critical_max:
                alerts.append({
                    'type': AlertType.HEART_RATE_HIGH if hr > self.hr_critical_max else AlertType.HEART_RATE_LOW,
                    'level': AlertLevel.CRITICAL,
                    'message': f'Critical heart rate: {hr} BPM'
                })
                max_level = max(max_level, AlertLevel.CRITICAL)
            elif hr < self.hr_min or hr > self.hr_max:
                alerts.append({
                    'type': AlertType.HEART_RATE_HIGH if hr > self.hr_max else AlertType.HEART_RATE_LOW,
                    'level': AlertLevel.WARNING,
                    'message': f'Abnormal heart rate: {hr} BPM'
                })
                max_level = max(max_level, AlertLevel.WARNING)
        
        # Check SpO2
        spo2 = soldier_data.get('spo2', 0)
        if spo2 > 0:  # Only check if we have valid data
            if spo2 < self.spo2_critical_min:
                alerts.append({
                    'type': AlertType.SPO2_LOW,
                    'level': AlertLevel.CRITICAL,
                    'message': f'Critical oxygen level: {spo2}%'
                })
                max_level = max(max_level, AlertLevel.CRITICAL)
            elif spo2 < self.spo2_min:
                alerts.append({
                    'type': AlertType.SPO2_LOW,
                    'level': AlertLevel.WARNING,
                    'message': f'Low oxygen level: {spo2}%'
                })
                max_level = max(max_level, AlertLevel.WARNING)
        
        # Check temperature
        temp = soldier_data.get('temperature', 0)
        if temp > 0:  # Only check if we have valid data
            if temp < self.temp_critical_min or temp > self.temp_critical_max:
                alerts.append({
                    'type': AlertType.TEMPERATURE_HIGH if temp > self.temp_critical_max else AlertType.TEMPERATURE_LOW,
                    'level': AlertLevel.CRITICAL,
                    'message': f'Critical temperature: {temp:.1f}°C'
                })
                max_level = max(max_level, AlertLevel.CRITICAL)
            elif temp < self.temp_min or temp > self.temp_max:
                alerts.append({
                    'type': AlertType.TEMPERATURE_HIGH if temp > self.temp_max else AlertType.TEMPERATURE_LOW,
                    'level': AlertLevel.WARNING,
                    'message': f'Abnormal temperature: {temp:.1f}°C'
                })
                max_level = max(max_level, AlertLevel.WARNING)
        
        # Check humidity
        if soldier_data.get('humidityValid', False):
            humidity = soldier_data.get('humidity', 0)
            if humidity > 0:
                if humidity < self.humidity_critical_min or humidity > self.humidity_critical_max:
                    alerts.append({
                        'type': AlertType.HUMIDITY_HIGH if humidity > self.humidity_critical_max else AlertType.HUMIDITY_LOW,
                        'level': AlertLevel.CRITICAL,
                        'message': f'Critical humidity: {humidity:.1f}%'
                    })
                    max_level = max(max_level, AlertLevel.CRITICAL)
                elif humidity < self.humidity_min or humidity > self.humidity_max:
                    alerts.append({
                        'type': AlertType.HUMIDITY_HIGH if humidity > self.humidity_max else AlertType.HUMIDITY_LOW,
                        'level': AlertLevel.WARNING,
                        'message': f'Abnormal humidity: {humidity:.1f}%'
                    })
                    max_level = max(max_level, AlertLevel.WARNING)

        # Check pressure
        if soldier_data.get('pressureValid', False):
            pressure = soldier_data.get('pressure', 0)
            if pressure > 0:
                if pressure < self.pressure_critical_min or pressure > self.pressure_critical_max:
                    alerts.append({
                        'type': AlertType.PRESSURE_LOW if pressure < self.pressure_critical_min else AlertType.PRESSURE_HIGH,
                        'level': AlertLevel.CRITICAL,
                        'message': f'Critical pressure: {pressure:.1f} hPa'
                    })
                    max_level = max(max_level, AlertLevel.CRITICAL)
                elif pressure < self.pressure_min or pressure > self.pressure_max:
                    alerts.append({
                        'type': AlertType.PRESSURE_LOW if pressure < self.pressure_min else AlertType.PRESSURE_HIGH,
                        'level': AlertLevel.WARNING,
                        'message': f'Abnormal pressure: {pressure:.1f} hPa'
                    })
                    max_level = max(max_level, AlertLevel.WARNING)

        # Check battery (key is 'batteryVoltage' in camelCase)
        battery = soldier_data.get('batteryVoltage', 0)
        if battery > 0:
            if battery < self.battery_critical:
                alerts.append({
                    'type': AlertType.BATTERY_CRITICAL,
                    'level': self.battery_critical_level,
                    'message': f'Critical battery: {battery:.2f}V'
                })
                max_level = max(max_level, self.battery_critical_level)
            elif battery < self.battery_low:
                alerts.append({
                    'type': AlertType.BATTERY_LOW,
                    'level': self.battery_low_level,
                    'message': f'Low battery: {battery:.2f}V'
                })
                max_level = max(max_level, self.battery_low_level)
        
        # Check heat stress flag
        if soldier_data.get('heatStress', False):
            alerts.append({
                'type': AlertType.HEAT_STRESS,
                'level': self.heat_stress_critical,
                'message': 'Heat stress detected'
            })
            max_level = max(max_level, self.heat_stress_critical)
        
        return max_level, alerts
    
    def get_alert_color(self, alert_level: int) -> str:
        """Get color code for alert level"""
        colors = {
            AlertLevel.OK: '#4CAF50',        # Green
            AlertLevel.WARNING: '#FF9800',   # Orange
            AlertLevel.CRITICAL: '#F44336',  # Red
            AlertLevel.EMERGENCY: '#D32F2F'  # Dark Red
        }
        return colors.get(alert_level, '#4CAF50')
    
    def get_config_summary(self) -> Dict:
        """Get current alert configuration as dictionary"""
        return {
            'heart_rate': {
                'min': self.hr_min,
                'max': self.hr_max,
                'critical_min': self.hr_critical_min,
                'critical_max': self.hr_critical_max
            },
            'spo2': {
                'min': self.spo2_min,
                'critical_min': self.spo2_critical_min
            },
            'temperature': {
                'min': self.temp_min,
                'max': self.temp_max,
                'critical_min': self.temp_critical_min,
                'critical_max': self.temp_critical_max
            },
            'humidity': {
                'min': self.humidity_min,
                'max': self.humidity_max,
                'critical_min': self.humidity_critical_min,
                'critical_max': self.humidity_critical_max
            },
            'pressure': {
                'min': self.pressure_min,
                'max': self.pressure_max,
                'critical_min': self.pressure_critical_min,
                'critical_max': self.pressure_critical_max
            },
            'battery': {
                'low': self.battery_low,
                'critical': self.battery_critical
            }
        }


# Global instance
alert_manager = None

def get_alert_manager(config_file='alerts.conf'):
    """Get or create global alert manager instance"""
    global alert_manager
    if alert_manager is None:
        alert_manager = AlertManager(config_file)
    return alert_manager