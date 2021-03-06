import React from 'react';
import './StationShow.css';

class StationShow extends React.Component{

  render(){
    return(
      <div className="Stations">
        <h5>My Stations</h5>
        {!this.props.state.error && this.props.state.stations.map((item,i) =>
          <li key={i}>{item.StationId}</li>
        )}
        {this.props.state.error &&(
          <p>{this.props.state.error}</p>
        )}
      </div>
    );
  }
}

export default StationShow;
