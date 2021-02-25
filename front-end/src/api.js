import axios from 'axios';
import config from './config'
import qs from 'qs';

// Here we declare all the api calls to the backend

// some constant declarations to the axios framework
axios.defaults.baseURL = config.apiUrl;
axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';

// call to login and get token
// obj is the user object to login
export const postLoginToken = obj => {
  const url = '/login';
  const data = qs.stringify(obj);
  return axios.post(url, data);
}

// call to logout and release token
// obj is the token of the user to disconect
export const postLogout = obj => {
  const url = '/logout';
  const config = {
    headers: {
      'X-OBSERVATORY-AUTH': 'Bearer ' + obj
    }
  };
  return axios.post(url, null, config);
}
