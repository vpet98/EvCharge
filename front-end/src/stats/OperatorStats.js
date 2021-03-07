import React from 'react';
import TimeSeriesGraph from './TimeSeriesGraph.js';
import { getStationShow, getSessionsPerStation, getSessionsPerPoint } from '../api_comm/api.js';


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
      points: null,
      graph_object_type: null,
      graph_object: null,
      msg: "",
      number_of_stations: 0,
      error: "",
      graph_options: null
    };

    this.handleApiCommError = this.handleApiCommError.bind(this);
    this.handleStationBtn = this.handleStationBtn.bind(this);
    this.stationsGraphSwitch = this.stationsGraphSwitch.bind(this);
    this.pointsGraphSwitch = this.pointsGraphSwitch.bind(this);
    this.getData = this.getData.bind(this);
    this.graphSwitch = this.graphSwitch.bind(this);
  }

  // handle errors when communicating with api
  handleApiCommError = err => {
    if(err.response && err.response.data.status === 402){
      this.setState({
        msg: err.response.data.message,
        error: ""
      });
    }else{
      this.setState({
        msg: "Sorry. We got a problem",
        error: err.message
      });
    }
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
    .catch(this.handleApiCommError);
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
        graph_options: null
      });
    this.graphSwitch();
  }

  // handle click charging point performance button
  pointsGraphSwitch(e){
    let target_point = this.state.points.filter(point => {return point.PointId.toString() === e.target.name.toString()})[0];
    if(this.state.graph_object !== target_point)
      this.setState({
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
    this.setState({ graph_options: null, error: "", msg: "Fetching data..." });
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
          this.setState({ graph_options: res, msg: "" })
        }, 0)
      })
      .catch(this.handleApiCommError);
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
          this.setState({ graph_options: res, msg: "" });
        }, 0)
      })
      .catch(this.handleApiCommError);
    }
  }

  showStations(){
    return (
      <>
        {this.state.number_of_stations > 0 &&(
          <p>You have {this.state.number_of_stations} stations</p>
        )}
        {this.state.stations.map(station =>
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
        )}
      </>
    );
  }

  render(){
    return(
      <>
        <h5>Operator Stats</h5>
        {this.state.show_stations && (
          <>
            <p>{ this.state.msg }</p>
            {this.state.error !== null && (
              <div className="error"><p>{this.state.error}</p></div>
            )}
            {this.showStations()}
          </>
        )}
        {this.state.show_graph &&(
          <TimeSeriesGraph
            data_callback={this.getData}
            page_callback={this.graphSwitch}
            graph_options={this.state.graph_options}
            msg={this.state.msg}
            error={this.state.error}
            secondDataName="# of charges"
          />
        )}
      </>
    );
  }
}

export default OperatorStats;
