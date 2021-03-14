import React from 'react';
import TimeSeriesGraph from './TimeSeriesGraph.js';
import { getEvPerUser, getSessionsPerEv } from '../api_comm/api.js';
import AppiErrorHandler from '../api_comm/error_handling.js';

// the stats page for users component
class UserStats extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      show_evs: false,
      show_graph: false,
      evs: null,
      selected_ev: null,
      msg: "",
      number_of_evs: 0,
      error: "",
      graph_msg: "",
      graph_error: "",
      graph_options: null
    };

    this.getData = this.getData.bind(this);
    this.graphSwitch = this.graphSwitch.bind(this);
    this.evGraphSwitch = this.evGraphSwitch.bind(this);
  }

  // once the page is ready search stations
  componentDidMount(){
    this.setState({ msg: "Searching vehicles...", error: "" });
    getEvPerUser(this.props.user)
    .then(json => {
      setTimeout(() =>{
        this.setState({
          msg: "",
          number_of_evs: json.data.NumberOfVehicles,
          evs: json.data.VehiclesList,
          show_evs: true
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

  graphSwitch(){
    this.setState({
      msg: "",
      error: "",
      show_evs: !this.state.show_evs,
      show_graph: !this.state.show_graph,
    });
  }

  // handle click ev charges button
  evGraphSwitch(e){
    let target_ev = this.state.evs.filter(ev => {return ev.VehicleId === e.target.name})[0];
    this.setState({
      selected_ev: target_ev,
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
    let l = json.data.VehicleChargingSessionsList;
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
        return acc + (graph_kw ? session.EnergyDelivered : session.SessionCost)
      }, 0);
      return v;
    });
    return [axis_x, axis_y];
  }

  // a function to fetch data from the api and refresh graph_options
  getData({from_date, to_date, graph_kw}){
    this.setState({ graph_options: null, graph_error: "", graph_msg: "Fetching data..." });
    let req_obj = {
      EvId: this.state.selected_ev.VehicleId,
      fDate: from_date,
      tDate: to_date,
      token: this.props.user.token
    };
    getSessionsPerEv(req_obj)
    .then(json => {
      setTimeout(() => {
        let [axis_x, axis_y] = this.parseJsonToTimeseries(json, graph_kw);
        let res = {
          x_axis: axis_x.map(date =>  date.getDate() + '/' + (date.getMonth() + 1) + '/' + date.getFullYear()),
          y_axis: axis_y,
          x_axis_title: "Time",
          y_axis_title: graph_kw ? "Energy delivered" : "Cost of session",
          graph_title: graph_kw ? "Energy delivered" : "Charging sessions cost",
          graph_aggregate: graph_kw ? json.data.TotalEnergyConsumed : axis_y.reduce((a, b) => a + b, 0)
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
  }

  showEvs(){
    return(
      <>
        {this.state.number_of_evs > 0 &&(
          <p>You have {this.state.number_of_evs} electric vehicles</p>
        )}
        <div className="collection">
          {this.state.evs.map((ev, index) =>
            <div key={index}>
              <a
                type="button"
                href="#!"
                className="collection-item"
                name={ev.VehicleId}
                onClick={this.evGraphSwitch}
              >
                {ev.Brand + ' ' + ev.Model + ' ' + ev.Variant}
              </a>
            </div>
          )}
        </div>
      </>
    );
  }

  render(){
    return(
      <>
        <h5>User Stats</h5>
        <p>{ this.state.msg }</p>
        {this.state.error !== null && (
          <div className="error"><p>{this.state.error}</p></div>
        )}
        {this.state.show_evs && (
          this.showEvs()
        )}
        {this.state.show_graph &&(
          <TimeSeriesGraph
            data_callback={this.getData}
            page_callback={this.graphSwitch}
            graph_options={this.state.graph_options}
            msg={this.state.graph_msg}
            error={this.state.graph_error}
            secondDataName="cost"
          />
        )}

      </>
    );
  }

}

export default UserStats;
