import React from 'react';
import './ServicesDiv.css';
import { pages, user_roles } from './App.js';

// a div that holds links to other important pages
class ServicesDiv extends React.Component {
  constructor(props){
    super(props);
    this.handleStats = this.handleStats.bind(this);
    this.handleSearchStations = this.handleSearchStations.bind(this);
    this.handleBeginCharge = this.handleBeginCharge.bind(this);
    this.handleFinishCharge = this.handleFinishCharge.bind(this);
    this.handleStationsManage = this.handleStationsManage.bind(this);
  }

  handleStats(e){
    this.props.callback({
      page: pages.stats,
      user: this.props.user
    });
  }

  handleSearchStations(e){
    this.props.callback({
      page: pages.searchStations,
      user: this.props.user
    });
  }

  handleBeginCharge(e){
    this.props.callback({
      page: pages.begin_charge,
      user: this.props.user
    });
  }

  handleFinishCharge(e){
    this.props.callback({
      page: pages.finish_charge,
      user: this.props.user
    });
  }

  handleStationsManage(e){
    this.props.callback({
      page: pages.stations,
      user: this.props.user
    });
  }

  render(){
    let user_with_roles = this.props.user && this.props.user.hasOwnProperty('roles');
    return(
      <div className="services_div">
        <div className="statistics_div">
          <p>Statistical analysis</p>
          <button
            type="button"
            className="btn waves-effect waves-light stats_btn"
            name="show_statistics"
            onClick={this.handleStats}
          >
            Stats
          </button>
        </div>
        <div className="find_stations_div">
          <p>Find charging stations nearby</p>
          <button
            type="button"
            className="btn waves-effect waves-light search_btn"
            name="find_stations"
            onClick={this.handleSearchStations}
          >
            Search
          </button>
        </div>
        {user_with_roles && this.props.user.roles !== [user_roles.guest] && (
          <div className="charge_div">
            <p>Charge your electric vehicle</p>
            <button
              type="button"
              className="btn waves-effect waves-light charge_btn"
              name="charge"
              onClick={this.handleBeginCharge}
              >
              Begin Charging
            </button>
            <button
              type="button"
              className="btn waves-effect waves-light finish_btn"
              name="finish_charge"
              onClick={this.handleFinishCharge}
              >
              Finish Charging
            </button>
          </div>
        )}
        {user_with_roles && this.props.user.roles.includes(user_roles.operator) && (
          <div className="stations_div">
            <p>Manage your charging stations</p>
            <button
              type="button"
              className="btn waves-effect waves-light stations_btn"
              name="stations"
              onClick={this.handleStationsManage}
              >
              My Stations
            </button>
          </div>
        )}
      </div>
    );
  }
}

export default ServicesDiv;
