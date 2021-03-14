import React from 'react';
import { getHealthcheck } from '../api_comm/api.js';
import AppiErrorHandler from '../api_comm/error_handling.js';

// the stats page for admin component
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
      let handler = new AppiErrorHandler(err);
      this.setState({
        healthcheck_result: "Sorry. We got a problem",
        error: handler.getError()
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
          className="btn waves-effect waves-light"
          onClick={this.handleCheckButton}
        >
          Check System
        </button>
        <p>{this.state.healthcheck_result}</p>
        {this.state.error !== null && (
          <div className="error"><p>{this.state.error}</p></div>
        )}
      </>
    );
  }
}

export default AdminStats;
