import React from 'react';
import TimeSeriesGraph from './TimeSeriesGraph.js';
import { getStationShow, getSessionsPerStation, getSessionsPerPoint } from '../api_comm/api.js';
import AppiErrorHandler from '../api_comm/error_handling.js';
import './Stats.css';
import M from 'materialize-css';

// the stats page for operators component
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
      msg: "",
      number_of_stations: 0,
      error: "",
      graph_object_type: null,
      graph_object: null,
      graph_msg: "",
      graph_error: "",
      graph_options: null
    };

    this.stationsGraphSwitch = this.stationsGraphSwitch.bind(this);
    this.pointsGraphSwitch = this.pointsGraphSwitch.bind(this);
    this.getData = this.getData.bind(this);
    this.graphSwitch = this.graphSwitch.bind(this);
  }

  // once the page is ready search stations
  componentDidMount(){
    this.setState({ msg: "Searching stations...", error: "" });
    getStationShow(this.props.user)
    .then(json => {
      setTimeout(() =>{
        this.setState({
          msg: "",
          number_of_stations: json.data.NumberOfStations,
          stations: json.data.StationsList,
          show_stations: true
        });
      }, 0)
    })
    .catch(err => {
      let handler = new AppiErrorHandler(err);
      this.setState({
        msg: handler.getMessage(),
        error: handler.getError()
      });
    });
  }

  componentDidUpdate() {
    let collapsible = document.querySelectorAll(".collapsible");
    M.Collapsible.init(collapsible, { accordion: false });
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
        graph_options: null
      });
    this.graphSwitch();
  }

  // handle click charging point performance button
  pointsGraphSwitch(e){
    let idObj = JSON.parse(e.target.name)
    let stationId = idObj.StationId;
    let pointId = idObj.PointId;
    let target_station = this.state.stations.filter(station => station.StationId === stationId)[0];
    let target_point = target_station.PointsList.filter(point => point.PointId.toString() === pointId.toString())[0];
    if(this.state.graph_object !== target_point)
      this.setState({
        selected_station: target_station,
        graph_object: target_point,
        graph_object_type: this.graph_object_types.point,
        graph_options: null
      });
    this.graphSwitch();
  }

  parseJsonToTimeseries(json, graph_kw){
    // get the dates as an axis_x
    let start = new Date(json.data.PeriodFrom);
    let end = new Date(json.data.PeriodTo);
    let diffTime = Math.abs(end - start);
    let diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    let axis_x = [...Array(diffDays).keys()].map(i => {
        let d = new Date(start);
        d.setDate(start.getDate() + i)
        return d;
      });
    // get axis_y
    let l = json.data.ChargingSessionsList;
    let data = l.map(session => {
        session.StartedOn = new Date(session.StartedOn);
        return session;
      });
    let axis_y = axis_x.map(date => {
      let exactDateData = data.filter(s => {
        return (s.StartedOn.getDate() === date.getDate()
                && s.StartedOn.getMonth() === date.getMonth()
                && s.StartedOn.getYear() === date.getYear());
      });
      let v = exactDateData.reduce((acc, session) => {
        return acc + (graph_kw ? session.EnergyDelivered : 1)
      }, 0);
      return v;
    });
    return [axis_x, axis_y];
  }

  // a function to fetch data from the api and refresh graph_options
  getData({from_date, to_date, graph_kw}){
    this.setState({ graph_options: null, graph_error: "", graph_msg: "Fetching data..." });
    if(this.state.graph_object_type === this.graph_object_types.station){
      let req_obj = {
        StationId: this.state.graph_object.StationId,
        fDate: from_date,
        tDate: to_date,
        token: this.props.user.token
      };
      getSessionsPerStation(req_obj)
      .then(json => {
        setTimeout(() => {
          let res = {
            x_axis: json.data.SessionsSummaryList.map(p => p.PointID),
            y_axis: json.data.SessionsSummaryList.map(p => graph_kw ? p.EnergyDelivered : p.PointSessions),
            x_axis_title: "point Ids",
            y_axis_title: graph_kw ? "Energy delivered" : "Number of charging sessions",
            graph_title: graph_kw ? "Energy delivered at every point" : "Charging sessions at every point",
            graph_aggregate: graph_kw ? json.data.TotalEnergyDelivered : json.data.NumberOfChargingSessions
          }
          this.setState({ graph_options: res, graph_msg: "" })
        }, 0)
      })
      .catch(err => {
        let handler = new AppiErrorHandler(err);
        this.setState({
          graph_msg: handler.getMessage(),
          graph_error: handler.getError()
        });
      });
    }else if (this.state.graph_object_type === this.graph_object_types.point) {
      let req_obj = {
        StationId: this.state.selected_station.StationId,
        PointId: this.state.graph_object.PointId,
        fDate: from_date,
        tDate: to_date,
        token: this.props.user.token
      };
      getSessionsPerPoint(req_obj)
      .then(json => {
        setTimeout(() =>{
          let [axis_x, axis_y] = this.parseJsonToTimeseries(json, graph_kw);
          let res = {
            x_axis: axis_x.map(date =>  date.getDate() + '/' + (date.getMonth() + 1) + '/' + date.getFullYear()),
            y_axis: axis_y,
            x_axis_title: "Time",
            y_axis_title: graph_kw ? "Energy delivered" : "Number of charging sessions",
            graph_title: graph_kw ? "Energy delivered" : "Charging sessions",
            graph_aggregate: axis_y.reduce((a, b) => a + b, 0)
          }
          this.setState({ graph_options: res, graph_msg: "" });
        }, 0)
      })
      .catch(err => {
        let handler = new AppiErrorHandler(err);
        this.setState({
          graph_msg: handler.getMessage(),
          graph_error: handler.getError()
        });
      });
    }
  }

  showStations(){
    return (
      <>
        {this.state.number_of_stations > 0 &&(
          <p>You have {this.state.number_of_stations} stations</p>
        )}
        <ul className="collapsible expandable">
          {this.state.stations.map((station, index) =>
            <li key={index} className="collection-item">
              <div className="collapsible-header">
                <a href="#!">{station.Address}</a>
              </div>
              <div className="collapsible-body collection">
                <a
                  name={station.StationId}
                  href="#!"
                  className="collection-item active"
                  onClick={this.stationsGraphSwitch}
                >
                  total station performance
                </a>
                {station.PointsList && station.PointsList.map((point, index) =>
                  <a
                    key={index}
                    name={JSON.stringify({StationId: station.StationId, PointId : point.PointId})}
                    className="collection-item"
                    href="#!"
                    onClick={this.pointsGraphSwitch}
                  >
                    point {point.PointId}
                  </a>
                )}
              </div>
            </li>
          )}
        </ul>
      </>
    );
  }

  render(){
    return(
      <>
        <h5>Operator Stats</h5>
        <p>{ this.state.msg }</p>
        {this.state.error !== null && (
          <div className="error"><p>{this.state.error}</p></div>
        )}
        {this.state.show_stations && (
          this.showStations()
        )}
        {this.state.show_graph &&(
          <TimeSeriesGraph
            data_callback={this.getData}
            page_callback={this.graphSwitch}
            graph_options={this.state.graph_options}
            msg={this.state.graph_msg}
            error={this.state.graph_error}
            secondDataName="# of charges"
          />
        )}
      </>
    );
  }
}

export default OperatorStats;
