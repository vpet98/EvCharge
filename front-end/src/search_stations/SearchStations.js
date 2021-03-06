import React from 'react';
import './SearchStations.css';
import { pages } from '../app_essentials/App.js';
import { getStationsNearby } from '../api_comm/api.js';
import Map from './Map.js';

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
    if(this.state.latitude < -90 || this.state.latitude > 90){
      this.setState({ error: "Invalid coordinates: Latitude needs to be between this bounds: [-90, 90]" });
    }else if(this.state.longitude < -180 || this.state.longitude > 180){
      this.setState({ error: "Invalid coordinates: Longitude needs to be between this bounds: [-180, 180]" });
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
          if(err.response.data.message)
            this.setState({ msg: err.response.data.message });
          else
            this.setState({ error: err.message });
        });
      this.setState({ map_center: [this.state.latitude, this.state.longitude] });
    }
  }

  render(){
    return(
      <>
        <p>A searchStations page</p>
        <button
          type="button"
          name="home"
          onClick={this.handleHome}
        >
          Home
        </button>
        <div>
          <p>Latitude</p>
          <input
            type="number"
            name="latitude"
            field="latitude"
            placeholder="latitude"
            value={this.state.latitude}
            onChange={this.handleInput}
          />
          <p>Longitude</p>
          <input
            type="number"
            name="longitude"
            field="longitude"
            placeholder="longitude"
            value={this.state.longitude}
            onChange={this.handleInput}
          />
          <p>Radius (in km)</p>
          <input
            type="number"
            name="radius"
            field="radius"
            placeholder="radius"
            value={this.state.radius}
            onChange={this.handleInput}
          />
          <button
            type="button"
            name="search"
            onClick={this.handleSubmit}
          >
            Search
          </button>
          <p>{this.state.msg}</p>
          <p>{this.state.error}</p>
        </div>
        <Map
          center={this.state.map_center}
          zoom={13}
          stations={this.state.stations}
          userPosition={this.state.userPosition}
          changeUserPositionCallback={this.handleUserPosition}/>
      </>
    );
  }
}

export default SearchStations;