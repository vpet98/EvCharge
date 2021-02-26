import React from 'react';
import './ServicesDiv.css';
import { pages } from './App.js';

// a div that holds links to other important pages
class ServicesDiv extends React.Component {
  constructor(props){
    super(props);
    this.handleStats = this.handleStats.bind(this);
  }

  handleStats(e){
    this.props.callback({
      page: pages.stats,
      user: this.props.user
    });
  }

  render(){
    return(
      <div className="services_div">
        <div className="statistics_div">
          <p>Statistical analysis</p>
          <button
            type="button"
            name="show_statistics"
            onClick={this.handleStats}
          >
            stats
          </button>
        </div>
        <div className="find_stations_div">
          <p>Find charging stations nearby</p>
          <button type="button" name="find_stations">search</button>
        </div>
        <div className="charge_div">
          <p>Charge your electric vehicle</p>
          <button type="button" name="charge">charge</button>
        </div>
        <div className="stations_div">
          <p>Manage your charging stations</p>
          <button type="button" name="stations">inspect</button>
        </div>
      </div>
    );
  }
}

export default ServicesDiv;
