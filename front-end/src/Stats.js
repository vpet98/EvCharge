import React from 'react';
import './Stats.css';
import { pages, user_roles } from './App.js';
import { getHealthcheck } from './api.js';

// the stats page component
class Stats extends React.Component{
  constructor(props){
    super(props);
    this.handleHome = this.handleHome.bind(this);
  }

  // handle click home button
  handleHome(e){
    this.props.callback({
      page: pages.main,
      user: this.props.user
    });
  }

  render(){
    let showStats = this.props.user !== null && this.props.user.hasOwnProperty('role');
    return(
      <>
        <button
          type="button"
          name="home"
          onClick={this.handleHome}
        >
          Home
        </button>
        {!showStats &&(
          <GuestStats />
        )}
        {showStats && this.props.user.role === user_roles.admin &&(
          <AdminStats />
        )}
        {showStats && this.props.user.role === user_roles.operator &&(
          <OperatorStats user={this.props.user}/>
        )}
        {showStats && this.props.user.role === user_roles.user &&(
          <UserStats />
        )}
      </>
    );
  }
}

class GuestStats extends React.Component {
  render(){
    return(
      <>
        <h5>Guest Stats</h5>
        <p>{new Date().toString()}</p>
        <p>To access the ev or stations Stats you need to login first</p>
      </>
    );
  }
}

class AdminStats extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      healthcheck_result: "",
      error: ""
    };
    this.handleCheckButton = this.handleCheckButton.bind(this);
  }

  handleCheckButton(e) {
    this.setState({
      healthcheck_result: "",
      error: ""
    });
    getHealthcheck()
    .then(json => {
      setTimeout(() =>{
        this.setState({
          healthcheck_result: "Everything seems to work perfectly",
          error: ""
        });
      }, 0)
    })
    .catch(err => {
      this.setState({
        healthcheck_result: "Sorry. We got a problem",
        error: err.toString()
      });
    });
  }

  render(){
    return(
      <>
        <h5>Admin Stats</h5>
        <button
          type="button"
          name="healthcheck"
          onClick={this.handleCheckButton}
        >
          Check System
        </button>
        <p>{this.state.healthcheck_result}</p>
        <p>{this.state.error}</p>
      </>
    );
  }
}

const res_stations = {
  stations: ["address1", "address2", "address3"]
}
const res_points = {
  points: ["p1", "p2", "p3", "p4"]
}
class OperatorStats extends React.Component {
  constructor(props) {
    // TODO -- make a real call to the server to get stations
    super(props);
    var today = new Date();
    var lastMonth = new Date();
    lastMonth.setMonth(lastMonth.getMonth - 1);
    this.state = {
      show_stations: true,
      show_graph: false,
      selected_station: null,
      stations: res_stations.stations,
      points: res_points.points,
      switch_checked: false,
      fDate: today,
      tDate: lastMonth
    };
    this.handleStationBtn = this.handleStationBtn.bind(this);
    this.StationsGraphSwitch = this.StationsGraphSwitch.bind(this);
    this.onInputChange = this.onInputChange.bind(this);
  }

  // handle click station button
  handleStationBtn(e){
    if(this.state.selected_station !== e.target.name)
      this.setState({ selected_station: e.target.name });
    else
      this.setState({ selected_station: null });
  }

  // handle click total station performance button
  StationsGraphSwitch(e){
    this.setState({
      show_stations: !this.state.show_stations,
      show_graph: !this.state.show_graph
    });
  }

  showStations(){
    return this.state.stations.map(station =>
      <>
        <button
          type="button"
          name={station}
          onClick={this.handleStationBtn}
        >
          {station}
        </button>
        {this.state.selected_station === station &&(
          <div>
            <button
              type="button"
              name={station}
              onClick={this.StationsGraphSwitch}
            >
              total station performance
            </button>
            {this.state.points.map(pid =>
              <button
                type="button"
                name={pid}
                onClick={this.StationsGraphSwitch}
              >
                {pid} performance
              </button>
            )}
          </div>
        )}
      </>
    );
  }

  onInputChange(e){
    if(e.target.type === "checkbox"){
      const v = e.target.checked;
      this.setState({ switch_checked: v});
    }else{
      if(e.target.name === "fDate"){
        const v = e.target.value;
        this.setState({ fDate: new Date(v) });
      }
      else if(e.target.name === "tDate"){
        const v = e.target.value;
        this.setState({ tDate: new Date(v) });
      }
    }
  }

  showGraph(){
    return(
      <>
        <button
          type="button"
          name="showStations"
          onClick={this.StationsGraphSwitch}
        >
          Show Stations
        </button>
        <form>
          <div className="switch">
            <label className="switch">
              charges
              <input type="checkbox" onChange={this.onInputChange}/>
              <span className="lever"></span>
              Kw/h
            </label>
          </div>
          <label>From</label>
          <input type="date" name="fDate" className="datepicker" onChange={this.onInputChange}/>
          <label>To</label>
          <input type="date" name="tDate" className="datepicker" onChange={this.onInputChange}/>
        </form>
      </>
    );
  }

  render(){
    return(
      <>
        <h5>Operator Stats</h5>
        {this.state.show_stations &&(
          this.showStations()
        )}
        {this.state.show_graph &&(
          this.showGraph()
        )}
      </>
    );
  }
}

class UserStats extends React.Component {
  constructor(props) {
    super(props);
  }

  render(){
    return(
      <h5>User Stats</h5>
    );
  }
}

export default Stats;
