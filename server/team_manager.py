import re
from typing import Dict, List, Set
from configparser import ConfigParser

class TeamManager:
    """Manages soldier team assignments from configuration file"""
    
    def __init__(self, config_file='teams.conf'):
        self.config_file = config_file
        self.teams: Dict[str, Set[int]] = {}
        self.soldier_to_team: Dict[int, str] = {}
        self.load_teams()
    
    def load_teams(self):
        """Load team configuration from file"""
        try:
            config = ConfigParser()
            config.read(self.config_file)
            
            if 'teams' not in config:
                print(f"Warning: No [teams] section in {self.config_file}")
                return
            
            for team_name, soldier_spec in config['teams'].items():
                soldier_ids = self._parse_soldier_spec(soldier_spec)
                self.teams[team_name] = soldier_ids
                
                # Build reverse mapping
                for soldier_id in soldier_ids:
                    self.soldier_to_team[soldier_id] = team_name
                
                print(f"Loaded team '{team_name}': {len(soldier_ids)} soldiers")
        
        except Exception as e:
            print(f"Error loading teams from {self.config_file}: {e}")
    
    def _parse_soldier_spec(self, spec: str) -> Set[int]:
        """Parse soldier ID specification string
        
        Examples:
            "1,2,3,4,5" -> {1,2,3,4,5}
            "1-5" -> {1,2,3,4,5}
            "1-3,7,9-11" -> {1,2,3,7,9,10,11}
        """
        soldier_ids = set()
        
        # Remove whitespace
        spec = spec.replace(' ', '')
        
        # Split by comma
        parts = spec.split(',')
        
        for part in parts:
            if '-' in part:
                # Range specification
                try:
                    start, end = part.split('-')
                    start = int(start)
                    end = int(end)
                    soldier_ids.update(range(start, end + 1))
                except ValueError:
                    print(f"Warning: Invalid range specification '{part}'")
            else:
                # Single ID
                try:
                    soldier_ids.add(int(part))
                except ValueError:
                    print(f"Warning: Invalid soldier ID '{part}'")
        
        return soldier_ids
    
    def get_team_name(self, soldier_id: int) -> str:
        """Get team name for a soldier ID"""
        return self.soldier_to_team.get(soldier_id, 'Unassigned')
    
    def get_team_soldiers(self, team_name: str) -> Set[int]:
        """Get all soldier IDs in a team"""
        return self.teams.get(team_name, set())
    
    def get_all_teams(self) -> List[Dict]:
        """Get all teams with metadata"""
        return [
            {
                'name': team_name,
                'soldierIds': sorted(list(soldier_ids)),
                'count': len(soldier_ids)
            }
            for team_name, soldier_ids in self.teams.items()
        ]
    
    def get_soldiers_by_teams(self, soldier_data: List[Dict]) -> Dict[str, List[Dict]]:
        """Group soldiers by team"""
        grouped = {}
        
        # Initialize all teams
        for team_name in self.teams.keys():
            grouped[team_name] = []
        
        # Group soldiers
        for soldier in soldier_data:
            team_name = self.get_team_name(soldier['nodeId'])
            if team_name not in grouped:
                grouped[team_name] = []
            grouped[team_name].append(soldier)
        
        return grouped
    
    def reload(self):
        """Reload team configuration from file"""
        self.teams.clear()
        self.soldier_to_team.clear()
        self.load_teams()