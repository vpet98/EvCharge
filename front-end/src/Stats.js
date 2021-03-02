import React from 'react';
import './Stats.css';
import TimeSeriesGraph from './TimeSeriesGraph.js';
import { pages, user_roles } from './App.js';
import { getHealthcheck, getStationShow, getSessionsPerStation } from './api.js';

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

class OperatorStats extends React.Component {
  graph_object_types = {
    station: "station",
    point: "point"
  }
  constructor(props) {
    super(props);
    this.state = {
      show_stations: false,
      show_graph: false,
      selected_station: null,
      stations: null,
      points: null,
      graph_object_type: null,
      graph_object: null,
      graph_title: "",
      x_axis: null,
      y_axis: null,
      x_axis_title: "",
      y_axis_title: "",
      graph_aggregate: null,
      msg: "Searching stations...",
      error: ""
    };

    this.handleStationBtn = this.handleStationBtn.bind(this);
    this.stationsGraphSwitch = this.stationsGraphSwitch.bind(this);
    this.pointsGraphSwitch = this.pointsGraphSwitch.bind(this);
    this.getData = this.getData.bind(this);
    this.graphSwitch = this.graphSwitch.bind(this);
  }

  // once the page is ready search stations
  componentDidMount(){
    // TODO -- make a real call to the server to get stations and points
    getStationShow(this.props.user)
      .then(json => {
        setTimeout(() =>{
          this.setState({
            msg: 'You have ' + json.data.NumberOfStations + ' stations',
            stations: [{
                StationId: "5f69790300355e4c0105fe0a",
                Operator: "GreenLots",
                Address: "5991 Cattleridge Blvd",
                Country: "United States",
                Latitude: 27.300522,
                Longitude: -82.45103,
                CostPerKWh: 0.9,
                PointsList: [
                  {
                    PointId: 62033,
                    Power: 40.0,
                    CurrentType: "dc",
                    Port: "chademo",
                  },
                  {
                    PointId: 62034,
                    Power: 40.0,
                    CurrentType: "dc",
                    Port: "ccs",
                  }
                ]
              }],//json.data.StationsList,
          show_stations: true
          });
        }, 0)
      })
      .catch(err => {
        this.setState({
          msg: "Sorry. We got a problem",
          error: err.toString()
        });
      });
  }

  // handle click station button
  handleStationBtn(e){
    if(!this.state.selected_station || this.state.selected_station.StationId !== e.target.name){
      let target_station = this.state.stations.filter(station => {return station.StationId === e.target.name})[0];
      this.setState({
        points: target_station.PointsList,
        selected_station: target_station,
      });
    }else{
      this.setState({
        selected_station: null,
        points: null
      });
    }
  }

  graphSwitch(){
    this.setState({
      msg: "",
      error: "",
      show_stations: !this.state.show_stations,
      show_graph: !this.state.show_graph,
    });
  }

  // handle click total station performance button
  stationsGraphSwitch(e){
    let target_station = this.state.stations.filter(station => {return station.StationId === e.target.name})[0];
    if(this.state.graph_object !== target_station)
      this.setState({
        graph_object: target_station,
        graph_object_type: this.graph_object_types.station,
        graph_title: "",
        x_axis: null,
        y_axis: null,
        x_axis_title: "",
        y_axis_title: "",
        graph_aggregate: null
      });
    this.graphSwitch();
  }

  // handle click charging point performance button
  pointsGraphSwitch(e){
    let target_point = this.state.points.filter(point => {return point.PointId === e.target.name})[0];
    if(this.state.graph_object !== target_point)
      this.setState({
        graph_object: target_point,
        graph_object_type: this.graph_object_types.point,
        graph_title: "",
        x_axis: null,
        y_axis: null,
        x_axis_title: "",
        y_axis_title: "",
        graph_aggregate: null
      });
    this.graphSwitch();
  }

  // a function to fetch data from the api and refresh y_axis and y_axis
  getData({from_date, to_date, graph_kw}){
    // TODO make api calls to get data
    if(this.state.graph_object_type === this.graph_object_types.station){
      let req_obj = {
        StationId: this.state.graph_object.StationId,
        fDate: from_date,
        tDate: to_date,
        token: this.props.user.token
      };
      getSessionsPerStation(req_obj)
      .then(json => {
        setTimeout(() =>{
          let xs = json.data.SessionsSummaryList.map(p => p.PointID);
          let ys = json.data.SessionsSummaryList.map(p => graph_kw ? p.EnergyDelivered : p.PointSessions);
          let total = graph_kw ? json.data.TotalEnergyDelivered : json.data.NumberOfChargingSessions;
          this.setState({
            x_axis: xs,
            y_axis: ys,
            x_axis_title: "point Ids",
            y_axis_title: graph_kw ? "Energy delivered" : "Number of charging sessions",
            graph_title: graph_kw ? "Energy delivered at every point" : "Charging sessions at every point",
            graph_aggregate: total
          });
        }, 0)
      })
      .catch(err => {
        if(err.response.status === 402)
          this.setState({
            msg: err.response.data.message,
            error: ""
          });
        else
          this.setState({
            msg: "Sorry. We got a problem",
            error: err.toString()
          });
      });
    }else if (this.state.graph_object_type === this.graph_object_types.point) {

    }
  }

  showStations(){
    return this.state.stations.map(station =>
      <div key={station.StationId}>
        <button
          type="button"
          name={station.StationId}
          onClick={this.handleStationBtn}
        >
          {station.Address}
        </button>
        {this.state.selected_station === station &&(
          <div>
            <button
              type="button"
              name={station.StationId}
              onClick={this.stationsGraphSwitch}
            >
              total station performance
            </button>
            {this.state.points.map(point =>
              <button
                key={point.PointId}
                type="button"
                name={point.PointId}
                onClick={this.pointsGraphSwitch}
              >
                point {point.PointId}
              </button>
            )}
          </div>
        )}
      </div>
    );
  }

  render(){
    return(
      <>
        <h5>Operator Stats</h5>
        <p>{ this.state.msg }</p>
        <p>{ this.state.error }</p>
        {this.state.show_stations &&(
          this.showStations()
        )}
        {this.state.show_graph &&(
          <TimeSeriesGraph
            title={this.state.graph_title}
            x_axis={this.state.x_axis}
            y_axis={this.state.y_axis}
            x_axis_title={this.state.x_axis_title}
            y_axis_title={this.state.y_axis_title}
            graph_aggregate={this.state.graph_aggregate}
            data_callback={this.getData}
            page_callback={this.graphSwitch}
          />
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
