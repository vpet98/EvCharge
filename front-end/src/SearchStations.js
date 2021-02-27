import React from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import './SearchStations.css';
import { pages } from './App.js';
import { getStationsNearby } from './api.js';

class SearchStations extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      latitude: 34.050745,
      longitude: -118.081014,
      radius: 1000,
      stations: null,
      map_center: [34.050745, -118.081014],
      error: ""
    };
    this.handleHome = this.handleHome.bind(this);
    this.handleInput = this.handleInput.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
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
      // TODO -- this needs some serius fixing
      // let req_params = {
      //   token: this.props.user.token,
      //   latitude: this.state.latitude,
      //   longitude: this.state.longitude,
      //   radius: this.state.radius
      // }
      // getStationsNearby(req_params)
      //   .then(json => {
      //     setTimeout(() => {
      //       this.setState({ stations: json.data.Stations });
      //       console.log(this.state.stations);
      //     }, 0)
      //   })
      //   .catch(err =>{
      //     this.setState({ error: err.message });
      //   });
      this.setState({ stations: [
            {
                "StationId": "5f6978b800355e4c01059523",
                "Operator": "GreenLots",
                "Address": "2244 Walnut Grove Ave",
                "CostPerKWh": 0.4,
                "Latitude": 34.050743103027344,
                "Longitude": -118.08101654052734
            },
            {
                "StationId": "5f6978b800355e4c01059551",
                "Operator": "GreenLots",
                "Address": "2131 Walnut Grove Ave",
                "CostPerKWh": 0.8,
                "Latitude": 34.0533447265625,
                "Longitude": -118.08415985107422
            }
        ]
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
          <p>Radius</p>
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
          stations={this.state.stations}/>
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

function Map({center, zoom, stations}){
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
