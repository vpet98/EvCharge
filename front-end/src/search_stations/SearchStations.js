import React from 'react';
import './SearchStations.css';
import { pages } from '../app_essentials/App.js';
import { getStationsNearby } from '../api_comm/api.js';
import Map from './Map.js';
import AppiErrorHandler from '../api_comm/error_handling.js';

class SearchStations extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      latitude: 34.050745,
      longitude: -118.081014,
      radius: 1,
      stations: null,
      map_center: [34.050745, -118.081014],
      userPosition: null,
      msg: "",
      error: ""
    };
    this.handleHome = this.handleHome.bind(this);
    this.handleInput = this.handleInput.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleUserPosition = this.handleUserPosition.bind(this);
  }

  // handle click home button
  handleHome(e){
    this.props.callback({
      page: pages.main,
    });
  }

  // handle any change in input areas
  handleInput(e){
    const name = e.target.name;
    const value = e.target.value;
    this.setState({ [name]: value });
  }

  // handle user adding markers event
  handleUserPosition(e){
    let lat = e.latlng.lat;
    let lon = e.latlng.lng;
    this.setState({
      latitude: lat,
      longitude: lon,
      map_center: [lat, lon],
      userPosition: [lat, lon],
    }, () => this.handleSubmit());
  }

  // handle search stations nearby by making an api call
  // if everything is OK then it diplays on the map the stations found
  // else it updates the error message
  handleSubmit(e){
    this.setState({ msg: "", error: "" });
    if(this.state.latitude === ""){
      this.setState({ error: "Invalid coordinates: Enter a value for latitude" });
    }else if(this.state.longitude === ""){
      this.setState({ error: "Invalid coordinates: Enter a value for longitude" });
    }else if(this.state.latitude < -90 || this.state.latitude > 90){
      this.setState({ error: "Invalid coordinates: Latitude needs to be between these bounds: [-90, 90]" });
    }else if(this.state.longitude < -180 || this.state.longitude > 180){
      this.setState({ error: "Invalid coordinates: Longitude needs to be between these bounds: [-180, 180]" });
    }else if(this.state.radius < 0){
      this.setState({ error: "Radius needs to be positive" });
    }else{
      this.setState({ msg: "Searching stations..." });
      let req_params = {
        latitude: this.state.latitude,
        longitude: this.state.longitude,
        radius: this.state.radius * 1000 // transform km to meters
      }
      getStationsNearby(req_params)
        .then(json => {
          setTimeout(() => {
            this.setState({
              stations: json.data.Stations,
              msg: 'Found ' + json.data.Stations.length + ' stations'
             });
          }, 0)
        })
        .catch(err =>{
          this.setState({ stations: null });
          let handler = new AppiErrorHandler(err);
          this.setState({ msg: handler.getMessage(), error: handler.getError() });
        });
      this.setState({ map_center: [this.state.latitude, this.state.longitude] });
    }
  }

  render(){
    return(
      <>
        <h5>Find stations nearby</h5>
        <p>Click anywhere in the map to find stations</p>
        <p>{this.state.msg}</p>
        {this.state.error && (
          <div className="error"><p>{this.state.error}</p></div>
        )}
        <div className="row">
          <div className="col s3" style={{paddingLeft:"0px"}}>
            <ul className="side-menu">
              <li><p>Latitude</p></li>
              <li><input
                type="number"
                name="latitude"
                field="latitude"
                placeholder="latitude"
                value={this.state.latitude}
                onChange={this.handleInput}
              /></li>
              <li><p>Longitude</p></li>
              <li><input
                type="number"
                name="longitude"
                field="longitude"
                placeholder="longitude"
                value={this.state.longitude}
                onChange={this.handleInput}
              /></li>
              <li><p>Radius (in km)</p></li>
              <li><input
                type="number"
                name="radius"
                field="radius"
                placeholder="radius"
                value={this.state.radius}
                onChange={this.handleInput}
              /></li>
              <li><button
                type="button"
                name="search"
                className="btn waves-effect waves-light"
                onClick={this.handleSubmit}
              >
                Search
              </button></li>
            </ul>
          </div>
          <div className="col s9" style={{paddingRight:"0px"}}>
            <Map
            center={this.state.map_center}
            zoom={13}
            stations={this.state.stations}
            userPosition={this.state.userPosition}
            changeUserPositionCallback={this.handleUserPosition}/>
          </div>
        </div>
      </>
    );
  }
}

export default SearchStations;
