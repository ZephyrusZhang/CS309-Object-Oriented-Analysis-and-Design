<template>
  <div class="wrapper">
    <div style="margin-left: 730px; width: 225px;" class="title"><h1> Open Courses </h1></div>
    <div style="width: 1200px; margin-left: 250px; margin-top: 50px">
      <el-table :data="tableData" stripe border class="table">
        <el-table-column prop="courseName" label="CourseName"/>
        <el-table-column prop="courseCode" label="CourseCode"/>
        <el-table-column prop="language" label="Language"/>
        <el-table-column prop="teacher" label="Teach"/>
        <el-table-column prop="date" label="Date"/>
        <el-table-column prop="time" label="Time"/>
        <el-table-column prop="location" label="Location"/>
        <el-table-column prop="duration" label="Duration"/>
        <el-table-column label="Operation" width="230">
          <template #default="scope">
            <el-button type="primary" size="mini" round icon="el-icon-edit" @click="handleEdit(scope.row, scope.$index)">Edit</el-button>
            <el-popconfirm title="Confirm to delete?" @confirm="handleDelete(scope.$index)">
              <template #reference>
                <el-button type="danger" size="mini" round icon="el-icon-delete">Delete</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-button type="primary" style="margin-top: 20px; margin-left: 500px" @click="addClass" round icon="el-icon-plus">
        Add Class
      </el-button>
    </div>
  </div>

  <el-dialog class="dialog" v-model="dialogVisible" title="Add Course" :before-close="handleDialogClose" width="600px">
    <el-form :rules="rules" :model="form" ref="formRef">
      <el-form-item label="Course Name" prop="courseName">
        <el-input v-model="form.courseName"></el-input>
      </el-form-item>
      <el-form-item label="Course Code" prop="courseCode">
        <el-input v-model="form.courseCode"></el-input>
      </el-form-item>
      <el-form-item label="Language" prop="language">
        <el-checkbox-group v-model="form.language" @change="checkboxGroupChange">
          <el-checkbox label="Chinese" name="language"></el-checkbox>
          <el-checkbox label="English" name="language"></el-checkbox>
          <el-checkbox label="Bilingual" name="language"></el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label="Teacher" prop="teacher">
        <el-input v-model="form.teacher"></el-input>
      </el-form-item>
      <el-form-item label="Date" prop="date">
        <el-date-picker :disabled-date="disabledDate" v-model="form.date" type="date" placeholder="Please pick a date"
                        style="width: 100%"/>
      </el-form-item>
      <el-form-item label="Time" prop="time">
        <el-time-picker format="HH:mm" v-model="form.time" placeholder="Please pick a time" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="Location" prop="location">
        <el-select v-model="form.location" placeholder="Please select course location" style="width: 100%">
          <el-option label="Teaching Building No.1 Lecture Hall" value="Teaching Building No.1 Lecture Hall"/>
          <el-option label="Research Building Lecture Hall" value="Research Building Lecture Hall"/>
          <el-option label="Library Conference Hall and Activity Room"
                     value="Library Conference Hall and Activity Room"/>
        </el-select>
      </el-form-item>
      <el-form-item label="Duration" prop="duration">
        <el-input v-model="form.duration"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
          <span class="dialog-footer">
            <el-button type="info" @click="dialogVisible = false" round icon="el-icon-close">Cancel</el-button>
            <el-button type="success" round @click="handleSubmit" icon="el-icon-check">Submit</el-button>
          </span>
    </template>
  </el-dialog>
</template>

<script>
import {reactive} from "vue";
import {ElMessage} from "element-plus";

const nameRegex = '^[a-zA-Z]+$'
const courseCodeRegex = '(^[a-zA-Z]+[0-9]+$)|(^[0-9]+[a-zA-Z]+$)'
const durationRegex = '(^[1-9]\d*.[0-9]+)|(^[1-9]\d*)$'

const form = reactive({
  courseName: '',
  courseCode: '',
  language: [],
  teacher: '',
  date: '',
  time: '',
  location: '',
  duration: ''
})

let editMode = false
let editRowIndex;

function formatDate(date) {
  let month = date.getMonth() + 1
  let day = date.getDate()
  return `${date.getFullYear()}/${month < 10 ? `0${month}` : month}/${day < 10 ? `0${day}` : day}`
}

function formatTime(time) {
  return time.toTimeString().substring(0, 5)
}

function isCoursesConflict(time_a, time_b, duration_a, duration_b) {
  console.log(time_a, time_b, duration_a, duration_b)
  if (time_b > time_a) {
    let diff = time_b.getTime() - time_a.getTime()
    let hour = diff % (24 * 3600 * 1000) / (3600 * 1000)
    return !(hour >= duration_a)
  } else if (time_a > time_b) {
    let diff = time_a.getTime() - time_b.getTime()
    let hour = diff % (24 * 3600 * 1000) / (3600 * 1000)
    return !(hour >= duration_b)
  } else if (time_a === time_b) {
    return true
  }
  return false
}

function convertDateFromString(dateString) {
  dateString.replace('/', '-')
  return new Date(dateString)
}

function checkTeacherOneCoursePerDay(teacher, date, tableData) {
  for (let i = 0; i < tableData.length; i++) {
    if (tableData[i].teacher === teacher && tableData[i].date === date) {
      return false
    }
  }
  return true
}

function checkOneCoursePerDay(courseCode, date, tableData) {
  for (let i = 0; i < tableData.length; i++) {
    if (tableData[i].courseCode === courseCode && tableData[i].date === date) {
      return false
    }
  }
  return true
}

function checkCourseCode(courseName, courseCode, tableData) {
  for (let i = 0; i < tableData.length; i++) {
    if (tableData[i].courseName !== courseName && tableData[i].courseCode === courseCode) {
      ElMessage.error(`Conflict with course ${tableData[i].courseName}(${courseCode}). Different courses should have different course codes.`)
      return false
    }
  }
  return true
}

//region Form data validate functions
const validateCourseName = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('Please input course name'))
  }
  if (!(new RegExp(nameRegex).test(value))) {
    callback(new Error('Course name should only contain english letters'))
  }
  callback()
}

const validateCourseCode = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('Please input course code'))
  }
  if (!(new RegExp(courseCodeRegex).test(value))) {
    callback(new Error('Course code should be a combination of letters and numbers (e.g., CS309, 19SE)'))
  }
  callback()
}

const validateLanguage = (rule, value, callback) => {
  if (typeof value[0] == 'undefined') {
    callback(new Error('Please select one language the course will use'))
  }
  callback()
}

const validateTeacherName = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('Please input teacher name'))
  }
  if (!(new RegExp(nameRegex).test(value))) {
    callback(new Error('Teacher name should only contain english letters'))
  }
  callback()
}

const validateDate = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('Please select a date'))
  }
  callback()
}

const validateTime = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('Please select time'))
  }
  callback()
}

const validateLocation = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('Please select location'))
  }
  callback()
}

const validateDuration = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('Please input duration'))
  }
  if (!(new RegExp(durationRegex).test(value))) {
    callback(new Error('Duration must be a number'))
  }
  callback()
}
//endregion

function clearForm(form) {
  form.courseName = ''
  form.courseCode = ''
  form.language[0] = ''
  form.teacher = ''
  form.date = ''
  form.time = ''
  form.location = ''
  form.duration = ''
}

const rules = reactive({
  courseName: [{validator: validateCourseName, trigger: 'blur'}],
  courseCode: [{validator: validateCourseCode, trigger: 'blur'}],
  language: [{validator: validateLanguage, trigger: 'blur'}],
  teacher: [{validator: validateTeacherName, trigger: 'blur'}],
  date: [{validator: validateDate, trigger: 'blur'}],
  time: [{validator: validateTime, trigger: 'blur'}],
  location: [{validator: validateLocation, trigger: 'blur'}],
  duration: [{validator: validateDuration, trigger: 'blur'}]
})

export default {
  name: 'App',
  data() {
    return {
      dialogVisible: false,
      tableData: [
        {
          courseName: 'OOAD',
          courseCode: 'CS309',
          language: 'English',
          teacher: 'John',
          date: '2022/10/30',
          time: '19:00',
          location: 'Activity Room',
          duration: '3.0h'
        },
        {
          courseName: 'How to Study CS',
          courseCode: 'CS666',
          language: 'Bilingual',
          teacher: 'Bob',
          date: '2022/10/30',
          time: '16:20',
          location: 'Research Building Lecture Hall',
          duration: '2.5h'
        }
      ],
      form,
      rules
    }
  },
  methods: {
    addClass() {
      this.dialogVisible = true
    },
    handleDialogClose() {
      this.dialogVisible = false
    },
    disabledDate(time) {
      return time.getTime() < Date.now() - 8.64e7;
    },
    handleSubmit() {
      let isFormValid;
      this.$refs.formRef.validate((valid) => isFormValid = valid)

      if (!isFormValid) return

      if (editMode) {
        this.tableData[editRowIndex] = {
          courseName: this.form.courseName,
          courseCode: this.form.courseCode,
          language: this.form.language,
          teacher: this.form.teacher,
          date: formatDate(this.form.date),
          time: formatTime(this.form.time),
          location: this.form.location,
          duration: this.form.duration + "h"
        }
        editMode = false
        this.dialogVisible = false
        ElMessage.success('Class edit successfully')
        return
      }

      if (!checkCourseCode(this.form.courseName, this.form.courseCode, this.tableData)) {
        return
      }

      for (let i = 0; i < this.tableData.length; i++) {
       /* if (this.tableData[i].courseName !== this.form.courseName && this.tableData[i].courseCode === this.form.courseCode) {
          ElMessage.error('The course has already existed')
          return
        } else */if (this.tableData[i].location === this.form.location && isCoursesConflict(
            convertDateFromString(this.tableData[i].date + ' ' + this.tableData[i].time + ':00'),
            convertDateFromString(formatDate(this.form.date) + ' ' + formatTime(this.form.time) + ':00'),
            parseFloat(this.tableData[i].duration.replace('h', '')),
            parseFloat(this.form.duration)
        )) {
          ElMessage.error('Any two different courses cannot share one room at the same time')
          return
        }
      }

      if (!checkTeacherOneCoursePerDay(this.form.teacher, formatDate(this.form.date), this.tableData)) {
        ElMessage.error('Each teacher can take no more than one lecture per day')
        return
      }

      if (!checkOneCoursePerDay(this.form.courseCode, formatDate(this.form.date), this.tableData)) {
        ElMessage.error('One course is scheduled at most once a day')
        return
      }

      this.tableData.push({
        courseName: this.form.courseName,
        courseCode: this.form.courseCode,
        language: this.form.language,
        teacher: this.form.teacher,
        date: formatDate(this.form.date),
        time: formatTime(this.form.time),
        location: this.form.location,
        duration: this.form.duration + "h"
      })

      this.dialogVisible = false
      ElMessage.success('Class added successfully')
      clearForm(this.form)
    },
    handleEdit(row, index) {
      this.form = reactive({
        courseName: row.courseName,
        courseCode: row.courseCode,
        language: [row.language],
        teacher: row.teacher,
        date: convertDateFromString(row.date),
        time: convertDateFromString(row.date + ' ' + row.time + ':00'),
        location: row.location,
        duration: row.duration.replace('h', '')
      })
      this.dialogVisible = true

      editMode = true
      editRowIndex = index
    },
    handleDelete(index) {
      console.log(`delete ${index}`)
      this.tableData.splice(index, 1)
      ElMessage.success('Delete successfully')
    },
    checkboxGroupChange() {
      if (this.form.language.length > 1) {
        this.form.language.shift()
      }
    }
  }
}
</script>

<style>
.wrapper {
  background-image: linear-gradient(to bottom right, #FC466B, #3F5EFB);
  height: 100vh;
  overflow: hidden;
}

.table {
  background-color: white;
  margin: 50px auto;
  width: 500px;
  padding: 20px 20px;
  border: 2px solid #6c6c6c;
  border-radius: 10px;
}

.title {
  background: azure;
  border-radius: 5px;
}
</style>
