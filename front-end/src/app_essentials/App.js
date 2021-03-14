import React from 'react';
import './App.css';
import Banner from './Banner.js';
import ServicesDiv from './ServicesDiv.js';
import Footer from './Footer.js';
import Login from '../login/Login.js';
import Stats from '../stats/Stats.js';
import SearchStations from '../search_stations/SearchStations.js';
import ChargeVehicle from '../charge_vehicle/ChargeVehicle.js'
import FinishChargeVehicle from '../charge_vehicle/FinishChargeVehicle.js'
import StationsManage from '../manage_stations/StationsManage.js'

// an enumeration of all possible pages
export const pages = {
  main: "main",
  login: "login",
  stats: "stats",
  searchStations: "searchStations",
  begin_charge: "begin_charge",
  finish_charge: "finish_charge",
  stations: "stations"
};

// an enumeration of all possible user roles
export const user_roles = {
  guest: "guest",
  admin: "admin",
  operator: "operator",
  user: "user"
};

// the most general Component that manages the different pages
class App extends React.Component {
  constructor(props){
    super(props);
    this.state = {
      user: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : props.user,
      page: pages.main
    };
    this.changePage = this.changePage.bind(this);
  }

  // a function to change the state (page, user)
  changePage(props){
    if(props.hasOwnProperty("user") && props.user !== this.state.user)
      this.setState({ user: props.user });
    if(props.page !== this.state.page)
      this.setState({ page: props.page });
  }

  render(){
    return (
      <div className="container">
      {this.state.page === pages.main && (
        <div className="mainPage">
          <div className="content">
            <Banner user={this.state.user} callback={this.changePage}/>
            <h1>Ev Charge</h1>
            <ServicesDiv user={this.state.user} callback={this.changePage}/>
          </div>
          <Footer/>
        </div>
      )}
      {this.state.page === pages.login && (
        <Login callback={this.changePage}/>
      )}
      {this.state.page === pages.stats && (
        <div className="statsPage">
          <div className="content">
            <Banner user={this.state.user} callback={this.changePage}/>
            <Stats user={this.state.user} callback={this.changePage}/>
          </div>
          <Footer/>
        </div>
      )}
      {this.state.page === pages.searchStations &&(
        <div className="searchStationsPage">
          <div className="content">
            <Banner user={this.state.user} callback={this.changePage}/>
            <SearchStations user={this.state.user} callback={this.changePage}/>
          </div>
          <Footer/>
        </div>
      )}
      {this.state.page === pages.begin_charge &&(
        <div className="chargingPage">
          <div className="content">
            <Banner user={this.state.user} callback={this.changePage}/>
            <ChargeVehicle user={this.state.user} callback={this.changePage}/>
          </div>
          <Footer/>
        </div>
      )}
      {this.state.page === pages.finish_charge &&(
        <div className="finishChargingPage">
          <div className="content">
            <Banner user={this.state.user} callback={this.changePage}/>
            <FinishChargeVehicle user={this.state.user} callback={this.changePage}/>
          </div>
          <Footer/>
        </div>
      )}
      {this.state.page === pages.stations &&(
        <div className="stationsPage">
          <div className="content">
            <Banner user={this.state.user} callback={this.changePage}/>
            <StationsManage user={this.state.user} callback={this.changePage}/>
          </div>
          <Footer/>
        </div>
      )}
      </div>
    );
  }
}

export default App;
