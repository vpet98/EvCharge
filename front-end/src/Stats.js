import React from 'react';
import './Stats.css';
import TimeSeriesGraph from './TimeSeriesGraph.js';
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

class OperatorStats extends React.Component {
  graph_object_types = {
    station: "station",
    point: "point"
  }
  constructor(props) {
    super(props);
    // TODO -- make a real call to the server to get stations and points
    var res_stations = {
      stations: ["address1", "address2", "address3"]
    }
    var res_points = {
      points: ["p1", "p2", "p3", "p4"]
    }
    this.state = {
      show_stations: true,
      show_graph: false,
      selected_station: null,
      stations: res_stations.stations,
      points: res_points.points,
      graph_object_type: null,
      graph_object: null,
      graph_title: "",
      x_axis: null,
      y_axis: null,
      x_axis_title: "",
      y_axis_title: "",
      graph_aggregate: null,
      msg: "",
      error: ""
    };
    this.handleStationBtn = this.handleStationBtn.bind(this);
    this.stationsGraphSwitch = this.stationsGraphSwitch.bind(this);
    this.pointsGraphSwitch = this.pointsGraphSwitch.bind(this);
    this.getData = this.getData.bind(this);
    this.graphSwitch = this.graphSwitch.bind(this);
  }

  // handle click station button
  handleStationBtn(e){
    if(this.state.selected_station !== e.target.name)
      this.setState({ selected_station: e.target.name });
    else
      this.setState({ selected_station: null });
  }

  graphSwitch(){
    this.setState({
      show_stations: !this.state.show_stations,
      show_graph: !this.state.show_graph,
    });
  }

  // handle click total station performance button
  stationsGraphSwitch(e){
    this.setState({
      graph_object: e,
      graph_object_type: this.graph_object_types.station
    });
    this.graphSwitch();
  }

  // handle click charging point performance button
  pointsGraphSwitch(e){
    this.setState({
      graph_object: e,
      graph_object_type: this.graph_object_types.point
    });
    this.graphSwitch();
  }

  // a function to fetch data from the api and refresh y_axis and y_axis
  getData({from_date, to_date, graph_kw}){
    // TODO make api calls to get data
    if(this.state.graph_object_type === this.graph_object_types.station){
      // Collect data for all the charging points of this station
      let SessionsPerStation_res = {
                                      StationID: "5f6978b800355e4c01059581",
                                      Operator: "ChargePoint (Coulomb Technologies)",
                                      RequestTimestamp: "2021-02-26 02:01:28",
                                      PeriodFrom: "2019-12-07 00:00:00",
                                      PeriodTo: "2019-12-10 23:59:59",
                                      TotalEnergyDelivered: 4.438,
                                      NumberOfChargingSessions: 1,
                                      NumberOfActivePoints: 1,
                                      SessionsSummaryList: [
                                          {
                                              PointID: "5f6978b800355e4c01059581_15293",
                                              PointSessions: 1,
                                              EnergyDelivered: 4.438
                                          }
                                      ]
                                  };
      let xs = SessionsPerStation_res.SessionsSummaryList.map(p => p.PointID);
      let ys = SessionsPerStation_res.SessionsSummaryList.map(p => graph_kw ? p.EnergyDelivered : p.PointSessions);
      let total = graph_kw ? SessionsPerStation_res.TotalEnergyDelivered : SessionsPerStation_res.NumberOfChargingSessions;
      this.setState({
        x_axis: xs,
        y_axis: ys,
        x_axis_title: "point Ids",
        y_axis_title: graph_kw ? "Energy delivered" : "Number of charging sessions",
        graph_title: graph_kw ? "Energy delivered at every point" : "Charging sessions at every point",
        graph_aggregate: total
      });
    }else if (this.state.graph_object_type === this.graph_object_types.point) {

    }
  }

  showStations(){
    return this.state.stations.map(station =>
      <div key={station}>
        <button
          type="button"
          name={station}
          onClick={this.handleStationBtn}
        >
          {station}
        </button>
        <p>{ this.state.msg }</p>
        <p>{ this.state.error }</p>
        {this.state.selected_station === station &&(
          <div>
            <button
              type="button"
              name={station}
              onClick={this.stationsGraphSwitch}
            >
              total station performance
            </button>
            {this.state.points.map(pid =>
              <button
                key={pid}
                type="button"
                name={pid}
                onClick={this.pointsGraphSwitch}
              >
                {pid} performance
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
