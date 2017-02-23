import Vue from 'vue';

export const Message = Vue.component('speaker', {
  data: () => ({
    text: 'Hello world!',
    julietText: "It's only your name that is my enemy. What's in a name ? A rose by any other name would smell as sweet. Oh Romeo be some other name.",
    romeoText: "I will be Romeo no more.",
  }),
  template: `<div>\
    <p v-on:click="readText" >Click me and I will speak!</p>
    <input type="text" v-model="text"/>
    <p v-on:click="readShakespeare" >Click me and I will recite Shakespeare!</p>
  </div>`,
  methods: {
    readText: function() {
      window.speechSynthesis.speak(new SpeechSynthesisUtterance(this.text));
    },
    readShakespeare: function() {
      const julietSpeech = new SpeechSynthesisUtterance(this.julietText);
      julietSpeech.lang = "en-AU";
      window.speechSynthesis.speak(julietSpeech);

      const romeoSpeech = new SpeechSynthesisUtterance(this.romeoText);
      romeoSpeech.lang = 'en-US';
      window.speechSynthesis.speak(romeoSpeech);
    }
  }
});
