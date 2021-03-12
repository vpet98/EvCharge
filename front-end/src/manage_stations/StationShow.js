import React from 'react';
import './StationShow.css';

class StationShow extends React.Component{

  render(){
    return(
      <div className="Stations">
        <h5>My Stations</h5>
        <ul class="myul">
        {!this.props.state.error && this.props.state.stations.map((item,i) =>
          <li key={i}>{item.StationId}</li>
        )}
        </ul>
        {this.props.state.error !== null && (
          <div className="error"><p>{this.props.state.error}</p></div>
        )}
      </div>
    );
  }
}

export default StationShow;
