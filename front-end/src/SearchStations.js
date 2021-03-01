import React from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap, useMapEvents } from 'react-leaflet';
import './SearchStations.css';
import { pages } from './App.js';
import { getStationsNearby } from './api.js';

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
    this.setState({ error: "" });
    if(this.state.latitude < -90 || this.state.latitude > 90){
      this.setState({ error: "Invalid coordinates: Latitude needs to be between this bounds: [-90, 90]" });
    }else if(this.state.longitude < -180 || this.state.longitude > 180){
      this.setState({ error: "Invalid coordinates: Longitude needs to be between this bounds: [-180, 180]" });
    }else if(this.state.radius < 0){
      this.setState({ error: "Radius needs to be positive" });
    }else{
      let req_params = {
        latitude: this.state.latitude,
        longitude: this.state.longitude,
        radius: this.state.radius * 1000 // transform km to meters
      }
      getStationsNearby(req_params)
        .then(json => {
          setTimeout(() => {
            this.setState({ stations: json.data.Stations });
          }, 0)
        })
        .catch(err =>{
          if(err.response.data.message)
            this.setState({ error: err.response.data.message });
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

function ChangeView({ center, zoom }) {
  const map = useMap();
  map.setView(center, zoom);
  return null;
}

function UserMarker({position, callback}){
  useMapEvents({
    click(e) {
      callback(e);
    }
  })
  return (
    <>
      {position &&(
        <Marker
          key={position[0]}
          position={position}
          interactive={true}
        >
          <Popup>
            User pin
            <br />
          </Popup>
        </Marker>
      )}
    </>
  )
}

function Map({center, zoom, stations, userPosition, changeUserPositionCallback}){
  return(
    <div id="mapid" className="map_div" style={{height:"500px"}}>
      <MapContainer
        center={center}
        zoom={zoom}
        scrollWheelZoom={true}
        style={{height:"500px", width: "500px"}}
      >
      <ChangeView center={center} zoom={zoom} />
        <TileLayer
          attribution='&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <UserMarker position={userPosition} callback={changeUserPositionCallback}/>
        {stations && (
          stations.map(station =>
            <Marker key={station.StationId} position={[station.Latitude, station.Longitude]}>
              <Popup>
                {"Operator: " + station.Operator + "\n"
                +"Address: " + station.Address + "\n"
                +"CostPerKWh: " + station.CostPerKWh
                }<br />
              </Popup>
            </Marker>
          )
        )}
      </MapContainer>
    </div>
  );
}
