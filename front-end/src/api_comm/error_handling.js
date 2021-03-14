class AppiErrorHandler{
  constructor(error){
    this.error = error;
    this.msg = null;
    this.err_msg = null;
    this.http_code = null;

    this.catchApiErrors();
  }

  getMessage(){
    return this.msg;
  }

  getError(){
    return this.err_msg;
  }

  getHttpCode(){
    return this.http_code;
  }

  // a method to handle the error
  catchApiErrors(){
    if(!this.error) return;
    if (this.error.response){
      // Request made and server responded
      console.log(this.error.response.data);
      console.log(this.error.response.status);
      console.log(this.error.response.headers);
      this.msg = this.error.response.data.message;
    }else if (this.error.request){
      // The request was made but no response was received
      console.log(this.error.request);
      this.err_msg = this.error.message;
    }else{
      // Something happened in setting up the request that triggered an Error
      console.log('Error', this.error.message);
      this.err_msg = this.error.message;
    }
  }

}

export default AppiErrorHandler;
